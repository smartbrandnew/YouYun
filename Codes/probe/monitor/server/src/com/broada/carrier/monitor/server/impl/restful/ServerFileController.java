package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.broada.carrier.monitor.base.restful.BaseFileController;

@Controller
@RequestMapping("/v1/monitor/files")
public class ServerFileController extends BaseFileController {
}
