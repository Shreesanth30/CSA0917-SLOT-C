public class StaticLifecycleDemo {
    static class CampusGateSystem {
        private static int globalTicketSequence;
        private static int activeOccupancy = 0;

        static {
            globalTicketSequence = 1000;
            System.out.println("[Static Init] Ticket sequence initialized to " + globalTicketSequence);
        }

        public static String issueTicket(String gateName, String regNo) {
            globalTicketSequence++;
            activeOccupancy++;
            return String.format("%s-TKT-%d (%s) | Campus Occupancy: %d",
                    gateName, globalTicketSequence, regNo, activeOccupancy);
        }

        public static void releaseVehicle(String regNo) {
            if (activeOccupancy > 0) activeOccupancy--;
            System.out.println("Released " + regNo + " | Campus Occupancy: " + activeOccupancy);
        }
    }

    public static void main(String[] args) {
        System.out.println(CampusGateSystem.issueTicket("NORTH", "TN01AA1111"));
        System.out.println(CampusGateSystem.issueTicket("SOUTH", "TN02BB2222"));
        CampusGateSystem.releaseVehicle("TN01AA1111");
    }
}
