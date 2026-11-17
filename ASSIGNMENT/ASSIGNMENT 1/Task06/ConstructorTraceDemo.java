public class ConstructorTraceDemo {
    static class Vehicle {
        Vehicle(String regNo) {
            System.out.println("1. Vehicle(String) constructor executed for " + regNo);
        }
    }
    static class Car extends Vehicle {
        Car(String regNo) {
            super(regNo);
            System.out.println("2. Car(String) constructor executed");
        }
    }
    static class ElectricCar extends Car {
        ElectricCar(String regNo) {
            super(regNo);
            System.out.println("3. ElectricCar(String) constructor executed");
        }
    }
    public static void main(String[] args) {
        new ElectricCar("TN03EV3333");
    }
}
