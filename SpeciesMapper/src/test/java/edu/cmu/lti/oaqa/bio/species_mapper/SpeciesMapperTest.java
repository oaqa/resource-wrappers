package edu.cmu.lti.oaqa.bio.species_mapper;

import java.util.ArrayList;

import edu.cmu.lti.oaqa.bio.annotate.species_mapper.Species;
import edu.cmu.lti.oaqa.bio.annotate.species_mapper.SpeciesMapper;

public class SpeciesMapperTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpeciesMapper m = new SpeciesMapper();
		String[] testTerms = {"","acorn weevil","sloth","slug","tiger", "mouse", "zymv", "aaldnv", "yeast", "human", "homo sapiens"};
		
		for(String name:testTerms)
		{
			System.out.println("Names for \"" + name + "\"");
			ArrayList<Species> names = m.getProperName(name);
			if(names != null)
				for(Species s : names)
					System.out.println("\t" + s.getName());
		}
		
	}

}
