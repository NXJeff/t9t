/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.bpmn2.be.request;

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.IdentifierConverter.bpmnTenantIdToT9tTenantRef;
import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.IdentifierConverter.t9tTenantRefToBPMNTenantId;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.EventSubscriptionQuery;
import org.joda.time.LocalDateTime;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.EventSubscriptionDTO;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.SearchRequestMapper;
import com.arvatosystems.t9t.bpmn2.request.EventSubscriptionSearchRequest;

import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;

public class EventSubscriptionSearchRequestHandler extends AbstractBPMNRequestHandler<EventSubscriptionSearchRequest> {

    private final RuntimeService runtimeService = Jdp.getRequired(RuntimeService.class);

    private static final SearchRequestMapper<EventSubscriptionQuery, EventSubscription> queryMapping = new SearchRequestMapper<>();

    static {
        queryMapping.addFilterMapping("SubscriptionId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.eventSubscriptionId(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("processInstanceId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.processInstanceId(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("ActivityId", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.activityId(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("EventName", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.eventName(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addFilterMapping("EventType", UnicodeFilter.class, (fieldFilter, query) -> {
            if (fieldFilter.getEqualsValue() != null) {
                return query.eventType(fieldFilter.getEqualsValue());
            } else {
                return null;
            }
        });

        queryMapping.addSortMapping("Created", EventSubscriptionQuery::orderByCreated);
    }

    @Override
    protected ServiceResponse executeInWorkflowContext(RequestContext requestContext, EventSubscriptionSearchRequest request) throws Exception {
        final List<EventSubscription> eventSubscriptions = queryMapping.search(runtimeService.createEventSubscriptionQuery()
                                                                                             .tenantIdIn(t9tTenantRefToBPMNTenantId(requestContext.getTenantRef())),
                request);

        final ReadAllResponse<EventSubscriptionDTO, NoTracking> response = new ReadAllResponse<>();
        response.setDataList(eventSubscriptions.stream()
                                               .map(subscription -> map(subscription))
                                               .collect(toList()));
        response.setNumResults(Long.valueOf(response.getDataList()
                                                    .size()));
        return response;
    }

    private DataWithTrackingW<EventSubscriptionDTO, NoTracking> map(EventSubscription subscription) {

        final EventSubscriptionDTO dto = new EventSubscriptionDTO();

        dto.setSubscriptionId(subscription.getId());
        dto.setProcessInstanceId(subscription.getProcessInstanceId());
        dto.setActivityId(subscription.getActivityId());
        dto.setEventName(subscription.getEventName());
        dto.setEventType(subscription.getEventType());
        dto.setCreated(LocalDateTime.fromDateFields(subscription.getCreated()));

        final DataWithTrackingW<EventSubscriptionDTO, NoTracking> result = new DataWithTrackingW<>();
        result.setTenantRef(bpmnTenantIdToT9tTenantRef(subscription.getTenantId()));
        result.setData(dto);
        result.setTracking(new NoTracking());

        return result;
    }
}
