package catalog;

import java.util.ArrayList;
import java.util.List;

public class AudioFeatures implements Feature{

  private List<String> composer = new ArrayList<>();
  private int length;
  private List<String> performers = new ArrayList<>();
  private String title;

  Validators valid = new Validators();

  public AudioFeatures(String title, int length, List<String> performers) {
    if (valid.isBlank(title)) {
      throw new IllegalArgumentException("Title must be not empty or null!");
    }
    this.title = title;
    if (length < 1) {
      throw new IllegalArgumentException("Length is not valid!");
    }
    this.length = length;
    if (valid.isEmpty(performers)) {
      throw new IllegalArgumentException("Performer list is not valid!");
    }
    this.performers = performers;
  }

  public AudioFeatures(String title, int length, List<String> composer, List<String> performers) {
    if (valid.isBlank(title)) {
      throw new IllegalArgumentException("Title must be not empty or null!");
    }
    this.title = title;
    if (length < 1) {
      throw new IllegalArgumentException("Length is not valid!");
    }
    this.length = length;
    if (valid.isEmpty(performers)) {
      throw new IllegalArgumentException("Performers list is not valid!");
    }
    this.performers = performers;
    this.composer = composer;
  }

  public int getLength() {
    return length;
  }

  public List<String> getContributors() {
    List<String> composersAndPerformers = new ArrayList<>();
    composersAndPerformers.addAll(performers);
    composersAndPerformers.addAll(composer);
    return composersAndPerformers;
  }

  public String getTitle() {
    return title;
  }

}
