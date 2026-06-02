package Poi.Stock.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class AssignedCodeHolder {

	private List<String> assignedCodes = new ArrayList<>();
}