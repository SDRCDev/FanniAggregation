package org.sdrc.datum19.document;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter
@Getter
public class SubGroup {
	private String _id;
	private String name;
	private List<String> keyWords;
}
