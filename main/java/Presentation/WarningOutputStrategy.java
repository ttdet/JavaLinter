package Presentation;

import Domain.Warning;

import java.util.List;
import java.util.Map;

public interface WarningOutputStrategy {
    void outputWarnings(List<Warning> warnings, Map<String, Object> config);
}
