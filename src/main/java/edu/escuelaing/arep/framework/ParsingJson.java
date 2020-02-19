package edu.escuelaing.arep.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ParsingJson
 */
public class ParsingJson {

	/**
	 * entra un objeto y retorna un json con sus atributos
	 * @param obj
	 * @return
	 * @throws JsonProcessingException
	 */
  	public static String toJson(Object obj) throws JsonProcessingException{
    	ObjectMapper mapper = new ObjectMapper();
		String jsonString = mapper.writeValueAsString(obj);
		return jsonString;
  	}
}