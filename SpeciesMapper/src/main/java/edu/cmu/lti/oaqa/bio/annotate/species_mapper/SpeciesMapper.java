package edu.cmu.lti.oaqa.bio.annotate.species_mapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class SpeciesMapper {
  private ArrayList<String[]> speciesList = new ArrayList<String[]>();

  /**
   * Constructs a new SpeciesMapper, reading in the species list from the default location: the
   * directory where the SpeciesMapper java file is located.
   */
  public SpeciesMapper() {
    InputStream in = getClass().getResourceAsStream("specs.txt");
    readData(in);
  }

  /**
   * Constructs a new SpeciesMapper object, reading in the species data from the location provided.
   * 
   * @param filePath
   *          Path to the Species Data File. This is a csv file of the form "commonName,properName"
   */
  public SpeciesMapper(String filePath) {
    try {
      File inFile = new File(filePath);
      readData(new FileInputStream(inFile));
    } catch (FileNotFoundException e) {
      System.err
              .println("ERROR: Could not find the Species Data File!\nMake sure it is in the proper directory.");
      e.printStackTrace();
    }
  }

  private void readData(InputStream f) {
    Scanner in = new Scanner(f);
    while (in.hasNextLine()) {
      String inputline = in.nextLine();
      String common = inputline.substring(0, inputline.indexOf(",")).trim();
      String proper = inputline.substring(inputline.indexOf(",") + 1).trim();
      String[] entry = { common, proper };
      speciesList.add(entry);
    }
  }

  /**
   * Maps common names of species to their proper scientific names
   * 
   * @param commonName
   *          name common name of species
   * @return the proper names of the species in an array of strings, or null.
   */
  public ArrayList<Species> getProperName(String commonName) {
    // binary search
    int mid = find(commonName, speciesList);
    if (mid < 0) {
      return null;
    }
    // linsearch to get range
    int begin = mid;
    while (begin >= 0 && speciesList.get(begin)[0].equals(commonName)) {
      begin--;
    }
    begin++;
    int end = mid;
    while (end < speciesList.size() && speciesList.get(end)[0].equals(commonName)) {
      end++;
    }
    // build array
    ArrayList<Species> species = new ArrayList<Species>();
    for (int i = begin; i < end; i++) {
      species.add(new Species(speciesList.get(i)[1]));
    }
    // return array
    return species;
  }

  // Binary search over the arrayList
  // TODO: check this for timeing, may be O(n log n) if arraylist is
  // linkedList.
  private static int find(String common, ArrayList<String[]> a) {
    int lo = 0;
    int hi = a.size() - 1;
    while (lo <= hi) {
      // Key is in a[lo..hi] or not present.
      int mid = lo + (hi - lo) / 2;
      int compare = common.compareTo(a.get(mid)[0]);
      if (compare < 0)
        hi = mid - 1;
      else if (compare > 0)
        lo = mid + 1;
      else
        return mid;
    }
    return -1;
  }
}
