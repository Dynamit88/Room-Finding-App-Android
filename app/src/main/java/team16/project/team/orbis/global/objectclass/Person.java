package team16.project.team.orbis.global.objectclass;


import com.google.firebase.database.DataSnapshot;

public class Person {

    private final String id;
    private final String name;
    private final String office;

    public Person(String id, String name, String office) {
        this.id = id;
        this.name = name;
        this.office = office;
    }

    public static Person setupPersonFromFirebase(DataSnapshot personData) {
        String id = personData.getKey();
        String name = personData.child("name").getValue().toString();
        String office = personData.child("office").getValue().toString();

        return new Person(id, name, office);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOffice() {
        return office;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (id != null ? !id.equals(person.id) : person.id != null) return false;
        if (name != null ? !name.equals(person.name) : person.name != null) return false;
        return office != null ? office.equals(person.office) : person.office == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (office != null ? office.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("%-10s %-30s", office, name);
    }
}
