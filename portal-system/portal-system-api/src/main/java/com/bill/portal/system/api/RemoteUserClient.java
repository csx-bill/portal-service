package com.bill.portal.system.api;

import com.bill.portal.common.core.model.R;
import com.bill.portal.common.core.security.UserInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/system/user")
public interface RemoteUserClient {

    @GetExchange("/info/{username}")
    R<UserInfo> info(@PathVariable("username") String username);

}
