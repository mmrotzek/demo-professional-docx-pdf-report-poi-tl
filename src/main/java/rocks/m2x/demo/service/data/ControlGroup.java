package rocks.m2x.demo.service.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ControlGroup {
    String nr;
    String name;
    List<Control> controls;
}
