package com.fullstack.commonservice.bmad.nguoi3.query.inventory;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A truly zero-field class fails Jackson's default FAIL_ON_EMPTY_BEANS check
 * when Axon serializes the query message ("Unable to serialize object").
 * This marker field exists purely to give Jackson something to serialize -
 * there is no actual filter criteria for "find all".
 */
@Data
@NoArgsConstructor
public class FindAllInventoryItemsQuery {

    private final boolean marker = true;
}
