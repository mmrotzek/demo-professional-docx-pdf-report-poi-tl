package rocks.m2x.demo.service.report.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Control {
    String nr;
    String name;
    String description;

    boolean applicable;
}
