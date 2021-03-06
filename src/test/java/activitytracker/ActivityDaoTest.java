package activitytracker;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mariadb.jdbc.MariaDbDataSource;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityDaoTest {

  ActivityDao activityDao;


  Activity activity1 = new Activity(LocalDateTime.of(2021, 02, 13, 10, 13), "Going to play basketball", ActivityType.BASKETBALL);
  Activity activity2 = new Activity(LocalDateTime.of(2021, 02, 13, 14, 13), "Biking in the town", ActivityType.BIKING);
  Activity activity3 = new Activity(LocalDateTime.of(2021, 02, 16, 11, 13), "Hiking in the town", ActivityType.HIKING);
  Activity activity4 = new Activity(LocalDateTime.of(2021, 02, 20, 16, 13), "Running in the wood", ActivityType.RUNNING);

  TrackPoint tp1 = new TrackPoint(LocalDate.of(2021,02,13), 47.15, 28.35);
  TrackPoint tp2 = new TrackPoint(LocalDate.of(2021,03,15), 48.15, 28.45);
  TrackPoint tp3 = new TrackPoint(LocalDate.of(2021,04,17), 49.15, 28.55);
  TrackPoint tp4 = new TrackPoint(LocalDate.of(2021,05,19), 47.55, 28.65);

  @BeforeEach
  public void init() {
    MariaDbDataSource dataSource;

    try {
      dataSource = new MariaDbDataSource();
      dataSource.setUrl("jdbc:mariadb://localhost:3306/activitytracker?useUnicode=true");
      dataSource.setUser("activitytracker");
      dataSource.setPassword("activitytracker");
    }
    catch (SQLException se) {
      throw new IllegalStateException("Can not create data source!", se);
    }

    Flyway flyway = Flyway.configure().dataSource(dataSource).load();

    flyway.clean();
    flyway.migrate();

    activityDao = new ActivityDao(dataSource);
    activityDao.saveActivity(activity1);
    activityDao.saveActivity(activity2);

  }

  @Test
  public void saveActivity() {
    activityDao.saveActivity(activity3);
    Activity testActivity = activityDao.saveActivity(activity4);

    assertEquals("Running in the wood", testActivity.getDesc());

  }

  @Test
  public void testById() {
    long id = activityDao.saveActivity(activity3).getId();
    String description = activityDao.findActivityById(id).getDesc();
    assertEquals("Hiking in the town", description);

  }

  @Test
  public void findActivityById() {
    Activity activity = activityDao.findActivityById(1L);

    assertEquals(ActivityType.BASKETBALL, activity.getType());
  }

  @Test
  public void listActivities() {
    List<Activity> temp = activityDao.listActivities();

    assertEquals(2, temp.size());
  }

  @Test
  public void selectActivitiesBeforeDate() {

    System.out.println(activityDao.selectActivitiesBeforeDate(LocalDate.of(2021, 2, 14)));
  }

  @Test
  public void findByIdTest() {
    Activity activity5 = new Activity(LocalDateTime.of(2021, 02, 24, 16, 13), "Running in Sheerwood", ActivityType.RUNNING);
    Activity result = activityDao.saveActivity(activity5);

    assertEquals(3, result.getId());
  }

}