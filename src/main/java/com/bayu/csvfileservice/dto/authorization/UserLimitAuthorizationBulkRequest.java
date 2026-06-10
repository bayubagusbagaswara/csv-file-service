package com.bayu.csvfileservice.dto.authorization;

import com.bayu.csvfileservice.dto.InputIdentifierRequest;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLimitAuthorizationBulkRequest extends InputIdentifierRequest {

    private List<UserLimitAuthorizationRequest> items;

}