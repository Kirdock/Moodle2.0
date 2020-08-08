package validation;

import java.util.List;

public interface IValidator {

    List<? extends Violation> validate(String fileDir);
}
