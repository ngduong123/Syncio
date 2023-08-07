package online.syncio.controller;

import com.mongodb.client.FindIterable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import online.syncio.component.SearchedUserCard;
import online.syncio.dao.MongoDBConnect;
import online.syncio.dao.PostDAO;
import online.syncio.model.LoggedInUser;
import online.syncio.model.Post;
import online.syncio.model.UserIDAndDateAndText;
import online.syncio.view.NotificationsPanel;

public class NotificationController {

    private NotificationsPanel pnlNoti;

    private static PostDAO postDAO = MongoDBConnect.getPostDAO();

    private final String CONFIG_FILE_PATH = "/online/syncio/config/config.properties";
    private static final String DESIRED_DATETIME_KEY = "desiredDateTime";
    private Date desiredDateTime;

    private FindIterable<Post> postList;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Format of your date-time string

    public NotificationController(NotificationsPanel pnlNoti) {
        this.pnlNoti = pnlNoti;
    }

    public void readDesiredDateTime(String userID) {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getResourceAsStream(CONFIG_FILE_PATH)) {
            properties.load(inputStream);
            String desiredDateTimeStr = properties.getProperty(userID + "." + DESIRED_DATETIME_KEY);
            if (desiredDateTimeStr != null) {
                desiredDateTime = dateTimeFormat.parse(desiredDateTimeStr);
                System.out.println("Desired date and time for user " + userID + ": " + desiredDateTime);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void writeDesiredDateTime(String userID, Date date) {
        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getResourceAsStream(CONFIG_FILE_PATH)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        properties.setProperty(userID + "." + DESIRED_DATETIME_KEY, dateTimeFormat.format(date));

        try (OutputStream outputStream = new FileOutputStream("src/" + CONFIG_FILE_PATH)) {
            properties.store(outputStream, "Desired Date and Time for Users");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to display notifications based on the desiredDateTime
    public void displayNotifications() {
        pnlNoti.getPnlResult().removeAll();

        String currentUserID = LoggedInUser.getCurrentUser().getId().toString();
        postList = postDAO.getAllByUserIDFindIterable(currentUserID);

        readDesiredDateTime(currentUserID);

        for (Post post : postList) {
            List<UserIDAndDateAndText> comments = post.getCommentList();
            Collections.reverse(comments);

            int nums = countNewComments(comments);

            if (nums > 0) {
                pnlNoti.getPnlResult().add(createNotificationCard(post, nums));
            }
        }

        pnlNoti.getPnlResult().revalidate();
        pnlNoti.getPnlResult().repaint();
    }

    private int countNewComments(List<UserIDAndDateAndText> comments) {
        int nums = 0;

        for (UserIDAndDateAndText comment : comments) {
            String commentDateTimeStr = comment.getDate();

            if (commentDateTimeStr != null) {
                try {
                    Date commentDateTime = dateTimeFormat.parse(commentDateTimeStr);

                    if (desiredDateTime != null && commentDateTime.after(desiredDateTime)) {
                        nums++;
                    } else {
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        return nums;
    }

    private SearchedUserCard createNotificationCard(Post post, int newCommentsCount) {
        String notificationText = newCommentsCount + " new comments on your post";
        String notificationDate = post.getCommentList().get(0).getDate();

        return new SearchedUserCard(post.getId().toString(), notificationText, notificationDate);
    }

}