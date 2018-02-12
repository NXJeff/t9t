package com.arvatosystems.t9t.base.jpa.impl;

import javax.persistence.EntityManager;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudCompositeKeyResponse;
import com.arvatosystems.t9t.base.jpa.IEntityMapper42;
import com.arvatosystems.t9t.base.jpa.IResolverCompositeKey42;

import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.CompositeKeyBase;
import de.jpaw.bonaparte.pojos.api.CompositeKeyRef;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.util.ApplicationException;

public class AbstractCrudCompositeKey42RequestHandler <
        KEY extends CompositeKeyRef,
        DTO extends CompositeKeyBase,
        TRACKING extends TrackingBase,
        REQUEST extends CrudCompositeKeyRequest<KEY, DTO, TRACKING>,
        ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
        > extends AbstractCrudAnyKey42RequestHandler<KEY, DTO, TRACKING, REQUEST, ENTITY>{

    // execute function of the interface description, but additional parameters
    // required in order to work around type erasure
    public CrudCompositeKeyResponse<KEY, DTO, TRACKING> execute(IEntityMapper42<KEY, DTO, TRACKING, ENTITY> mapper,
            IResolverCompositeKey42<KEY, KEY, TRACKING, ENTITY> resolver, REQUEST crudRequest) {

        // fields are set as required
        validateParameters(crudRequest, crudRequest.getKey() == null);

        CrudCompositeKeyResponse<KEY, DTO, TRACKING> rs = new CrudCompositeKeyResponse<KEY, DTO, TRACKING>();
        ENTITY result;

        EntityManager entityManager = jpaContextProvider.get().getEntityManager(); // copy it as we need it several times

        try {
            switch (crudRequest.getCrud()) {
            case CREATE:
                result = performCreate(mapper, resolver, crudRequest, entityManager);
                rs.setKey(result.ret$Key()); // just copy
                break;
            case READ:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                rs.setKey(crudRequest.getKey()); // just copy
                break;
            case DELETE:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                if (!resolver.writeAllowed(resolver.getTenantRef(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                validateDelete(result);
                entityManager.remove(result);
                break;
            case INACTIVATE:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                if (!resolver.writeAllowed(resolver.getTenantRef(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(false);
                break;
            case ACTIVATE:
                result = resolver.findActive(crudRequest.getKey(), crudRequest.getOnlyActive());
                if (!resolver.writeAllowed(resolver.getTenantRef(result))) {
                    throw new T9tException(T9tException.WRITE_ACCESS_ONLY_CURRENT_TENANT);
                }
                result.put$Active(true);
                break;
            case UPDATE:
                result = performUpdate(mapper, resolver, crudRequest, entityManager, crudRequest.getKey());
                break;
            case MERGE:
                // If the key is passed in and result already exist then perform update.
                if (recordExists(crudRequest.getKey(), resolver)) {
                    result = performUpdate(mapper, resolver, crudRequest, entityManager, crudRequest.getKey());
                } else {
                    result = performCreate(mapper, resolver, crudRequest, entityManager);
                    rs.setKey(result.ret$Key()); // just copy
                }
                break;
            default:
                throw new T9tException(T9tException.INVALID_CRUD_COMMAND);
            }
            if (crudRequest.getCrud() != OperationType.DELETE) {
                rs.setTracking(result.ret$Tracking()); // populate result
                rs.setData(postRead(mapper.mapToDto(result), result)); // populate
                // result
            }
            rs.setReturnCode(0);
            return rs;
        } catch (T9tException e) {
            // careful! Catching only ApplicationException masks standard T9tExceptions such as RECORD_INACTIVE or RECORD_NOT_FOUND!
            // We must return the original exception if we got a T9tException already!
            // Therefore this catch is essential!
            throw e;
        } catch (ApplicationException e) {
            throw new T9tException(T9tException.ENTITY_DATA_MAPPING_EXCEPTION, "Tracking columns: "
                    + e.toString());
        }
    }

    /** recordExists is used for the MERGE operation and next to existence of the record also ensures that the record
     * is of the current tenant. This implies that MERGE operations only work for the current tenant, which is OK
     * for all entities except the Tenant entity itself. The tenant however is not of key type "composite", but "surrogate".
     * @param key
     * @param resolver
     * @return
     */
    protected boolean recordExists(KEY key, IResolverCompositeKey42<KEY, KEY, TRACKING, ENTITY> resolver) {
        try {
            ENTITY entity = resolver.find(key);
            return entity != null && resolver.isOfMatchingTenant(entity);
        } catch(T9tException e) {
            return false;
        }
    }
}