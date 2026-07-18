package com.fullstack.commonservice.user.query;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FindAllUsersQuery {
    private final boolean marker = true;
}
