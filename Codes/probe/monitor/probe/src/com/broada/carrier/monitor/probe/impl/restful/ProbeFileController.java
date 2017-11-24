package com.broada.carrier.monitor.probe.impl.restful;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.broada.carrier.monitor.base.restful.BaseFileController;

@Controller
@RequestMapping("/v1/probe/files")

public class ProbeFileController extends BaseFileController {
}
