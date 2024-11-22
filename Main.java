import java.sql.*;
import java.util.Scanner;


public class Main {
    // sensitive variables, that's why we make them private static and final at the same time.
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "Mattjaezzy@11";

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // through this try and catch block, the drivers are being loaded. It can throw ClassNotFoundException for real.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        // Now through this try and catch block we will establish a connection with the database, it can throw SQLException for real.
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            while (true) {
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                Scanner scanner = new Scanner(System.in);

                System.out.println("1. Reserve a room");
                System.out.println("2. View Reservations");
                System.out.println("3. Get Room number ");
                System.out.println("4. Update Reservations");
                System.out.println("5. Delete Reservations");
                System.out.println("6. Exit");
                System.out.println("Choose an option: ");
                int choice = scanner.nextInt();

                // The connection is passed as a parameter to avoid creating instances unnecessarily, which could be inefficient as the number of users on a project increases.
                switch (choice) {
                    case 1:
                        reserveRoom(connection, scanner);
                        break;
                    case 2:
                        viewReservations(connection);
                        break;
                    case 3:
                        getRoomNumber(connection, scanner);
                        break;
                    case 4:
                        updateReservation(connection, scanner);
                        break;
                    case 5:
                        deleteReservation(connection, scanner);
                        break;
                    case 6:
                        exit(0);
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Try Again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reserveRoom(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter guest name: ");
            String guestName = scanner.next();
            scanner.nextLine();
            System.out.println("Enter room number: ");
            int roomNumber = scanner.nextInt();
            System.out.println("Enter contact number: ");
            String contactNumber = scanner.next();

            String sql = "INSERT INTO reservation (guest_name, room_number, contact_number) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, guestName);
                preparedStatement.setInt(2, roomNumber);
                preparedStatement.setString(3, contactNumber);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Reservation Successful!");
                } else {
                    System.out.println("Reservation Failed!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void viewReservations(Connection connection) throws SQLException {
        String sql = "SELECT reservation_id, guest_name, room_number, contact_number, reservation_date FROM reservation";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            System.out.println("Current Reservations:");
            System.out.println(" +--------------------------+-------------------------+-----------------+---------------------+-----------------------------------+");
            System.out.println(" | RESERVATION ID           |  GUEST                  | ROOM NUMBER     | CONTACT NUMBER      |   RESERVATION DATE                |");
            System.out.println(" +--------------------------+-------------------------+-----------------+---------------------+-----------------------------------+");

            while (resultSet.next()) {
                int reservationId = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_number");
                String contactNumber = resultSet.getString("contact_number");
                String reservationDate = resultSet.getTimestamp("reservation_date").toString();

                // Format and display the reservation data in a table-like format
                System.out.printf("| %-14d  | %-15s | %-13d | %-20s | %-19s |\n",
                        reservationId, guestName, roomNumber, contactNumber, reservationDate);
            }
            System.out.println(" +--------------------------+-------------------------+-----------------+---------------------+-----------------------------------+");
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Reservation ID:");
            int reservationId = scanner.nextInt();
            System.out.println("Enter Guest Name:");
            String guestName = scanner.next();

            String sql = "SELECT room_number FROM reservation " +
                    "WHERE reservation_id = ? AND guest_name = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, reservationId);
                preparedStatement.setString(2, guestName);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    int roomNumber = resultSet.getInt("room_number");
                    System.out.println("Room number for Reservation ID " + reservationId +
                            " and Guest " + guestName + " is: " + roomNumber);
                } else {
                    System.out.println("Reservation not found for the given ID and guest name.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Reservation ID to Update:");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consumes the newline character

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID!");
                return;
            }
            System.out.println("Enter new guest name:");
            String newGuestName = scanner.next();
            scanner.nextLine();
            System.out.println("Enter new room number:");
            int newRoomNumber = scanner.nextInt();
            System.out.println("Enter new contact number:");
            String newContactNumber = scanner.next();

            String sql = "UPDATE reservation SET guest_name = ?, room_number = ?, contact_number = ? " +
                    "WHERE reservation_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, newGuestName);
                preparedStatement.setInt(2, newRoomNumber);
                preparedStatement.setString(3, newContactNumber);
                preparedStatement.setInt(4, reservationId);

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Reservation Updated Successfully!");
                } else {
                    System.out.println("Reservation update failed");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteReservation(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Reservation ID to delete:");
            int reservationId = scanner.nextInt();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID");
                return;
            }

            String sql = "DELETE FROM reservation WHERE reservation_id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, reservationId);
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Reservation deleted Successfully");
                } else {
                    System.out.println("Reservation deletion failed!");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        try {
            String sql = "SELECT reservation_id FROM reservation WHERE reservation_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, reservationId);
                ResultSet resultSet = preparedStatement.executeQuery();
                return resultSet.next(); // If there is a result: the reservation exists!
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;  // Handle database errors as needed
        }
    }

    public static void exit(int status) throws InterruptedException {
        System.out.println("Exiting System");
        int i = 5;
        while (i != 0) {
            System.out.println(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println();
    }
}
