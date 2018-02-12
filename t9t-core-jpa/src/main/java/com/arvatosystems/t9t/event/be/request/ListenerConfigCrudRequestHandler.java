package com.arvatosystems.t9t.event.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.impl.ListenerConfigCache;
import com.arvatosystems.t9t.event.ListenerConfigDTO;
import com.arvatosystems.t9t.event.ListenerConfigRef;
import com.arvatosystems.t9t.event.jpa.entities.ListenerConfigEntity;
import com.arvatosystems.t9t.event.jpa.mapping.IListenerConfigDTOMapper;
import com.arvatosystems.t9t.event.jpa.persistence.IListenerConfigEntityResolver;
import com.arvatosystems.t9t.event.request.ListenerConfigCrudRequest;
import com.arvatosystems.t9t.event.services.ListenerConfigConverter;

import de.jpaw.dp.Jdp;

public class ListenerConfigCrudRequestHandler extends AbstractCrudSurrogateKey42RequestHandler<ListenerConfigRef, ListenerConfigDTO, FullTrackingWithVersion, ListenerConfigCrudRequest, ListenerConfigEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenerConfigCrudRequestHandler.class);

    private final IListenerConfigEntityResolver entityResolver = Jdp.getRequired(IListenerConfigEntityResolver.class);
    private final IListenerConfigDTOMapper sinksMapper = Jdp.getRequired(IListenerConfigDTOMapper.class);

    @Override
    public CrudSurrogateKeyResponse<ListenerConfigDTO, FullTrackingWithVersion> execute(RequestContext ctx, ListenerConfigCrudRequest crudRequest) throws Exception {
        // TODO: update for delete as well
        CrudSurrogateKeyResponse<ListenerConfigDTO, FullTrackingWithVersion> resp = execute(sinksMapper, entityResolver, crudRequest);
        final ListenerConfigDTO dto = resp.getData();
        if (dto != null) {
            switch (crudRequest.getCrud()) {
            case CREATE:
            case MERGE:
            case UPDATE:
            case ACTIVATE:
            case INACTIVATE:
                ctx.addPostCommitHook((ctx2, rq, rs) -> {
                    LOGGER.debug("Updating JPA entity listener config cache to {}", dto);
                    ListenerConfigCache.updateRegistration(dto.getClassification(), ctx.tenantRef, ListenerConfigConverter.convert(dto));
                });
                break;
            default:
                break;
            }
        }
        return resp;
    }
}