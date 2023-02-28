package Domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CheckManager {

    private static final Map<String, Check> CHECK_MAP = new HashMap<>();
    static {
        CHECK_MAP.put("CheckAbstractFactoryPattern", new CheckAbstractFactoryPattern());
        CHECK_MAP.put("CheckClassFieldsAccess", new CheckClassFieldsAccess());
        CHECK_MAP.put("CheckClassName", new CheckClassName());
        CHECK_MAP.put("CheckDecoratorPattern", new CheckDecoratorPattern());
        CHECK_MAP.put("CheckDRYPrinciple", new CheckDRYPrinciple());
        CHECK_MAP.put("CheckHollywoodPrinciple", new CheckHollywoodPrinciple());
        CHECK_MAP.put("CheckMethodName", new CheckMethodName());
        CHECK_MAP.put("CheckRedundantConstructor", new CheckRedundantConstructor());
        CHECK_MAP.put("CheckRedundantInterface", new CheckRedundantInterface());
        CHECK_MAP.put("CheckSingletonPattern", new CheckSingletonPattern());
        CHECK_MAP.put("CheckStrategyPattern", new CheckStrategyPattern());
        CHECK_MAP.put("CheckTrainWreck", new CheckTrainWreck());
    }

    public List<Warning> runAllChecks(ClassRegistry classRegistry, Map<String, Object> configMap) {
        List<Warning> warnings = new LinkedList<>();

        Map<String, Object> checks = (Map<String, Object>) configMap.get("checks");
        for (Map.Entry<String, Object> e: checks.entrySet()) {
            String checkName = e.getKey();
            boolean checkEnabled = false;

            try {
                checkEnabled = (boolean) e.getValue();
            } catch (ClassCastException cce) {
                System.err.format("Check toggles can only be true or false: %s\n\n", checkName);
                continue;
            }

            if (checkEnabled) {
                Check check = CHECK_MAP.get(checkName);

                if (check == null) {
                    System.err.format("Check does not exist: %s\n\n", checkName);
                    continue;
                }

                try {
                    warnings.addAll(check.check(classRegistry));
                } catch (Exception checkException) {
                    System.err.format("An error occured while performing check: %s\n\n", checkName);
                    checkException.printStackTrace(System.err);
                }
            }
        }

        return warnings;
    }

}
