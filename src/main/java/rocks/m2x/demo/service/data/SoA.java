package rocks.m2x.demo.service.data;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SoA {
    String created;
    String version;
    boolean draft;

    String company;

    List<ControlGroup> groups;
}
