package coronaproject;

import org.mariadb.jdbc.MariaDbDataSource;

import javax.sound.midi.Soundbank;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CoronaVaccinationMain {

  private CitizenValidation citizenValidation;

  public void setCitizenValidation(CitizenValidation citizenValidation) {
    this.citizenValidation = citizenValidation;
  }

  public static void main(String[] args) {

    CoronaVaccinationMain cvm = new CoronaVaccinationMain();
    cvm.setCitizenValidation(new CitizenValidation());
    MariaDbDataSource dataSource = new MariaDbDataSource();

    try {
      dataSource.setUrl("jdbc:mariadb://localhost:3306/corona?useUnicode=true");
      dataSource.setUser("coronavaccinadmin");
      dataSource.setPassword("coronavaccin");
    } catch (SQLException se) {
      throw new IllegalStateException("Can not connect to database!", se);
    }

    CoronaDao coronaDao = new CoronaDao(dataSource);

    Scanner scanner = new Scanner(System.in);
    int pushedMenu = 0;

    while (pushedMenu != 6) {

      System.out.println("1. Regisztráció");
      System.out.println("2. Tömeges regisztráció");
      System.out.println("3. Generálás");
      System.out.println("4. Oltás");
      System.out.println("5. Oltás meghiúsulás");
      System.out.println("6. Kilépés");
      System.out.println("Kérem, adja meg a kívánt tevékenység sorszámát!");

      pushedMenu = Integer.parseInt(scanner.nextLine());

      switch (pushedMenu) {
        case 1:
          System.out.println("A regisztrációt választotta!");
          System.out.println(cvm.registrate(coronaDao));
          break;
        case 2:
          System.out.println("A tömeges regisztrációt választotta fájlbeolvasással!");
          System.out.println("Kérem adja meg a fájl helyét");
        cvm.registerFromCvdFile(coronaDao);
          break;

        case 3:
          System.out.println("A 'generálás' menüt választotta!");
        cvm.generating(coronaDao);
          break;

        case 4:
          System.out.println("Az 'oltás' menüt választotta!");
        cvm.giveVaccin(coronaDao);
          break;

        case 5:
          System.out.println("Az 'oltás meghiúsulása' menüt' választotta!");
//        cvm.notGivenVaccin(coronaDao);
          break;

        case 6:
          System.out.println("Köszönöm közreműködését! Viszont látásra!");
//        byby();
          break;

        default:
          System.out.println("Ilyen menüpont nincs! Kérem, a lehetséges menüpontokból válasszon!");
      }
    }
  }

  private void giveVaccin(CoronaDao coronaDao) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Kérem adja meg a TAJ számot!");
    String taj = scanner.nextLine();
    citizenValidation.isTajValid(taj);
    Citizens citizens = coronaDao.searchCitizenAndVaccinationByTaj(taj);
    if (citizens != null && citizens.getNumberOfVaccination() == 2){
    System.out.println("Ezzel a TAJ számmal a beteg már megkapta a második oltást is!");

    } else if (citizens != null && citizens.getNumberOfVaccination() == 1 && !citizens.getLastVaccination().plusDays(15).isAfter(LocalDateTime.now())) {
      System.out.println("Az első oltás időpontja: " + citizens.getLastVaccination() + ", az oltás fajtája: " + citizens.getVaccinations().get(0).getVaccinType() + " volt.");
      System.out.println("Mivel az első oltástól számítva még nem telt el 15 nap, így a második oltás még nem adható be!");

    } else if (citizens != null || citizens.getNumberOfVaccination() == 1 && citizens.getLastVaccination().plusDays(15).isBefore(LocalDateTime.now())) {
      System.out.println("Az első oltás időpontja: " + citizens.getLastVaccination() + ", az oltás fajtája: " + citizens.getVaccinations().get(0).getVaccinType() + " volt.");
      System.out.println("Kérem adja meg az oltás dátumát (éééé-hh-nn formátumban)!");
      LocalDate vaccinDate = LocalDate.parse(scanner.nextLine());
      System.out.println(vaccinDate);
      System.out.println("(1. Pfizer; 2. AstraZeneca; 3. Moderna; 4. Sputnik V; 5. Sinopharm)");
      System.out.println("Kérem adja meg a vakcina sorszámát!");
      VaccinType vaccin = witchVaccin(Integer.parseInt(scanner.nextLine()));
      System.out.println("A(z) " + vaccin.toString() + " vakcinát választotta!");
      coronaDao.vaccination(vaccinDate, taj, vaccin);
    }

  }

  private VaccinType witchVaccin(int vaccin) {
    return VaccinType.values()[vaccin - 1];
  }

  private Citizens registrate(CoronaDao coronaDao) {
//    CitizenValidation cv = new CitizenValidation();
    Scanner scanner = new Scanner(System.in);
    System.out.println("Kérem, adja meg az oltásra regisztáló adatait!");
    System.out.println("Teljes neve: ");
    String fullName = scanner.nextLine();
    citizenValidation.isValidCitizenName(fullName);
//    cv.isValidCitizenName(fullName);
    System.out.println("Lakhelyének irányítószáma:");
    int zip = Integer.parseInt(scanner.nextLine());
    String city = coronaDao.findTownWithTheGivenZip(zip);
    citizenValidation.isValidPostcode(zip);
//    cv.isValidPostcode(zip);
    if (city.isEmpty()) {
      throw new IllegalArgumentException("Nincs ilyen irányítószámmal település!");
    }
    System.out.println("Város: " + coronaDao.findTownWithTheGivenZip(zip));
    System.out.println("Az oltásra regisztáló életkora:");
    int age = Integer.parseInt(scanner.nextLine());
    citizenValidation.isValidAge(age);
//    cv.isValidAge(age);
    System.out.println("Az oltásra regisztáló e-mail címe:");
    String email1 = scanner.nextLine();
    if (email1 == null || email1.length() < 4 || !email1.contains("@")) {
      throw new IllegalArgumentException("Nem megfelelő e-mail cím!");
    }
    System.out.println("Kérem, adja meg ismét az e-mail címet:");
    String email2 = scanner.nextLine();
    if (!email1.equals(email2)) {
      throw new IllegalArgumentException("Nem egyező e-mail címek!");
    }
    System.out.println("Az oltásra regisztáló TAJ-száma:");
    String taj = scanner.nextLine();
    citizenValidation.isTajExists(coronaDao, taj);
    citizenValidation.isTajValid(taj);
//    cv.isTajValid(taj);

    Citizens citizens = new Citizens(fullName, zip, age, email1, email2, taj);
    coronaDao.addCitizenToDatabase(citizens);
    return citizens;
  }


  public void registerFromCvdFile(CoronaDao coronaDao) {
    Scanner scanner = new Scanner(System.in);
    String file = scanner.nextLine();
    try (BufferedReader reader = Files.newBufferedReader(Path.of(file))) {
      GroupRegistration gr = new GroupRegistration();
      List<Citizens> citizens = gr.readRegistrationDataFromFile(reader);
      coronaDao.insertGroupOfCitizens(citizens);
    } catch (IOException ioe) {
      throw new IllegalArgumentException("Can not read file", ioe);
    }
  }

  public void generating(CoronaDao coronaDao) {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Kérem adja meg a listázandó település irányítószámát!");
    int postalCode = Integer.parseInt(scanner.nextLine());
    System.out.println("Kérem adja meg, hogy milyen néven mentsem el a generált fájlt!");
    String fileName = scanner.nextLine();
    List<Citizens> citizensInGivenTown = coronaDao.findCitizensWithGivenPostalCode(postalCode);
    generateFile(fileName, citizensInGivenTown);
//    System.out.println(Arrays.asList(citizensInGivenTown));

  }

  private void generateFile(String fileName, List<Citizens> citizens) {
    List<Citizens> temp = new ArrayList<>();
    LocalDateTime firstTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(8,0));
    for (Citizens tempCitizen : citizens) {
      tempCitizen.setLastVaccination(firstTime);
      temp.add(tempCitizen);
      firstTime = firstTime.plusMinutes(30);
    }
//    System.out.println(Arrays.asList(temp));
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Path.of(fileName)))) {
      writer.println("Időpont;Név;Irányítószám;Életkor;E-mail cím;TAJ szám");
      for (Citizens tmp : temp) {
        writer.print(tmp.getLastVaccination().getHour() + ":");
        writer.printf("%02d", tmp.getLastVaccination().getMinute());
        writer.print(";" + tmp.getFullName() + ";");
        writer.print(tmp.getZip() + ";");
        writer.print(tmp.getAge() + ";");
        writer.print(tmp.getEmail() + ";");
        writer.print(tmp.getTaj() + "\n");
      }
    } catch (IOException ioe) {
      throw new IllegalStateException("Can not write file!", ioe);
    }
  }

}
