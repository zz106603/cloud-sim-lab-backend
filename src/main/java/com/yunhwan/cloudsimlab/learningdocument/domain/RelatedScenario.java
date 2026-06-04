package com.yunhwan.cloudsimlab.learningdocument.domain;

import com.yunhwan.cloudsimlab.scenario.domain.Scenario;

public record RelatedScenario(Scenario scenario, String reason) {
}
