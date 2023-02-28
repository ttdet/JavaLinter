package Domain;

import DataSource.ClassData;

import java.io.File;
import java.util.List;

public interface Check {
    List<Warning> check(ClassRegistry classRegistry);
}
