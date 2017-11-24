package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.broada.carrier.monitor.base.restful.BasePolicyController;

@Controller
@RequestMapping("/v1/monitor/policies")
public class ServerPolicyController extends BasePolicyController {
}
