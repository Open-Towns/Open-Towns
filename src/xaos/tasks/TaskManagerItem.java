package xaos.tasks;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

public final class TaskManagerItem implements Externalizable {

    private static final long serialVersionUID = -6261978696270872236L;

    private Task task;
    private ArrayList<Integer> listCitizens;

    public TaskManagerItem() {
    }

    public TaskManagerItem(Task task) {
        setTask(task);
        listCitizens = new ArrayList<Integer>();
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public ArrayList<Integer> getListCitizens() {
        return listCitizens;
    }

    public void setListCitizens(ArrayList<Integer> listCitizens) {
        this.listCitizens = listCitizens;
    }

    public boolean containsCitizen(int citizenID) {
        return listCitizens.contains(Integer.valueOf(citizenID));
    }

    public void addCitizen(int citizenID) {
        listCitizens.add(Integer.valueOf(citizenID));
    }

    public void removeCitizen(int citizenID) {
        listCitizens.remove(Integer.valueOf(citizenID));
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer("Task\n");
        buffer.append(task.toString());
        buffer.append("\n Cits: ");
        buffer.append(listCitizens.size());
        buffer.append("\n");
        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        task = (Task) in.readObject();
        listCitizens = (ArrayList<Integer>) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(task);
        out.writeObject(listCitizens);
    }
}
