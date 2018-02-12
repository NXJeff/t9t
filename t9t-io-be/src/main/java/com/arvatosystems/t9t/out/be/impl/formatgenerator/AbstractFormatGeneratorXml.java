package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

public class AbstractFormatGeneratorXml extends AbstractFormatGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormatGeneratorXml.class);
    protected final IStandardNamespaceWriter namespaceWriter = Jdp.getRequired(IStandardNamespaceWriter.class);
    protected static ConcurrentHashMap<String, JAXBContext> jaxbContexts = new ConcurrentHashMap<String, JAXBContext>(16);

    protected JAXBContext context = null;
    protected Marshaller m = null;
    protected XMLStreamWriter writer = null;

    protected String xmlDefaultNamespace;
    protected String xmlNamespacePrefix;
    protected String xmlRecordName;
    protected String xmlRootElementName;
    protected Boolean writeTenantId;

    protected void doWriteTenantId() throws XMLStreamException, JAXBException {
        JAXBElement<String> element = new JAXBElement<String>(new QName(xmlDefaultNamespace, "tenantId", xmlNamespacePrefix), String.class, tenantId);
        m.marshal(element, writer);
        nl();
    }

    protected void writeCustomElement(String id) throws XMLStreamException, JAXBException {
        Map<String, Object> map = outputSessionParameters.getAdditionalParameters();
        if (map != null) {
            Object value = map.get(id);
            if (value != null && value instanceof String) {
                JAXBElement<String> element = new JAXBElement<String>(new QName(xmlDefaultNamespace, id, xmlNamespacePrefix), String.class, (String)value);
                m.marshal(element, writer);
                nl();
            }
        }
    }

    protected void writeNamespaces() throws XMLStreamException {
        writer.setPrefix("bon", "http://www.jpaw.de/schema/bonaparte.xsd");
        namespaceWriter.writeApplicationNamespaces(writer);
    }

    protected void nl() throws XMLStreamException {
        writer.writeCharacters("\n");
    }

    @Override
    protected void openHook() throws IOException, ApplicationException {
        super.openHook();
        // get the context
        final String path   = sinkCfg.getJaxbContextPath();
        xmlDefaultNamespace = sinkCfg.getXmlDefaultNamespace();         // http:something
        xmlNamespacePrefix  = sinkCfg.getXmlNamespacePrefix();
        xmlRecordName       = sinkCfg.getXmlRecordName();               // records, instance field name or List element
        xmlRootElementName  = sinkCfg.getXmlRootElementName();          // simple name of xml root element class
        writeTenantId       = sinkCfg.getWriteTenantId();               // write the tenantId field?

        if (path == null) {
            throw new T9tIOException(T9tIOException.NO_JAXB_CONTEXT_PATH, sinkCfg.getDataSinkId());
        }
        context = jaxbContexts.get(path);
        try {
            if (context == null) {
                context = JAXBContext.newInstance(path);
                jaxbContexts.putIfAbsent(path, context);
            }
            m = context.createMarshaller();
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tIOException(T9tIOException.XML_SETUP_ERROR, ExceptionUtil.causeChain(e));
        }
        try {
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);      // does not work with the XMLStreamWriter without a separate indenting writer
            m.setProperty(Marshaller.JAXB_ENCODING, encoding.name());
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        } catch (PropertyException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tIOException(T9tIOException.XML_SET_PROPERTY_ERROR, ExceptionUtil.causeChain(e));
        }
        try {
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

            writer = factory.createXMLStreamWriter(outputResource.getOutputStream());
            if (xmlDefaultNamespace != null)
                writer.setDefaultNamespace(xmlDefaultNamespace);

            writer.writeStartDocument();
            nl();

            writeNamespaces();

            writer.writeStartElement(xmlDefaultNamespace, sinkCfg.getXmlRootElementName());
            nl();
            if (Boolean.TRUE.equals(writeTenantId))
                doWriteTenantId();
        } catch (XMLStreamException | FactoryConfigurationError | JAXBException e1) {
            LOGGER.error(e1.getMessage(), e1);
            throw new T9tIOException(T9tIOException.XML_MARSHALLING_ERROR, e1.getClass().getSimpleName() + ": " + e1.getMessage());
        }
    }

    @Override
    public void generateData(int recordNo, int mappedRecordNo, long recordId, BonaPortable record) throws IOException, ApplicationException {
        try {
            JAXBElement<BonaPortable> element = new JAXBElement<BonaPortable>(new QName(xmlDefaultNamespace, xmlRecordName, xmlNamespacePrefix), (Class<BonaPortable>) record.getClass(), record);
            m.marshal(element, writer);
            nl();
        } catch (JAXBException | XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tIOException(T9tIOException.XML_MARSHALLING_ERROR, ExceptionUtil.causeChain(e));
        }
    }

    @Override
    public void close() throws IOException, ApplicationException {
        try {
            writer.writeEndDocument();
            nl();  // here woodstox breaks with some NPE!
            writer.close();
        } catch (XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tIOException(T9tIOException.XML_MARSHALLING_ERROR, ExceptionUtil.causeChain(e));
        }
    }
}