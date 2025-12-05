package org.manager;

import lombok.Getter;
import lombok.Setter;
import org.utilities.SaveHandler;

public class ControllerManager {
    @Getter
    @Setter
    private static SaveHandler activeSaveHandler;

}
