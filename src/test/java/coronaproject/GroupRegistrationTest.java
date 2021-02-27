package coronaproject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupRegistrationTest {

  BufferedReader reader;

  @BeforeEach
  public void createBufferedReader() {
    try {
      reader = Files.newBufferedReader(Path.of("src/main/resources/groupRegister.csv"));

    } catch (IOException ioe) {
      throw new IllegalStateException("Can not read file!", ioe);
    }

  }

  @Test
  public void testFillRegistrationDataFromFile() {

      GroupRegistration gr = new GroupRegistration();
      gr.readRegistrationDataFromFile(reader);

      List<Citizens> citizens = gr.getCitizens();
      System.out.println(Arrays.asList(citizens));

      assertEquals("Jane Doe", citizens.get(1).getFullName());
  }
}