package com.shadowfang.core.infoboard;

import java.util.List;

public interface Program {
    String getName();
    List<String> render(InfoBoard board);
}
