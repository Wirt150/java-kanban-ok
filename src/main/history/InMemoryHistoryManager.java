package history;

import taskarea.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList customTasksHistory;

    public InMemoryHistoryManager() {
        this.customTasksHistory = new CustomLinkedList();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            customTasksHistory.add(task);
        }
    }

    @Override
    public void remove(int id) {
        customTasksHistory.remove(id);
    }

    public void updateInfo(Task task) {
        customTasksHistory.updateInfo(task);
    }

    @Override
    public List<Task> getHistory() {
        return customTasksHistory.getTasks();
    }

    private static class CustomLinkedList {
        private final Map<Integer, Node<Task>> nodeMap;
        private Node<Task> head;
        private Node<Task> tail;

        public List<Task> getTasks() {
            List<Task> taskList = new ArrayList<>();
            if (head != null) {
                Node<Task> taskNode = head;
                while (taskNode.getNext() != null) {
                    taskList.add(taskNode.getData());
                    taskNode = taskNode.getNext();
                }
                taskList.add(taskNode.getData());
            }
            return taskList;
        }

        private CustomLinkedList() {
            this.nodeMap = new HashMap<>();
            this.head = null;
            this.tail = null;
        }

        public void add(Task task) {
            if (nodeMap.containsKey(task.getId())) {
                removeNode(nodeMap.get(task.getId()));
            }
            linkLast(task);
        }

        public void updateInfo(Task task) {
            if (nodeMap.containsKey(task.getId())) {
                Node<Task> node = nodeMap.get(task.getId());
                node.setData(task);
            }

        }

        public void remove(int id) {
            if (nodeMap.containsKey(id)) {
                removeNode(nodeMap.get(id));
                nodeMap.remove(id);
            }
        }

        private void linkLast(Task task) {
            Node<Task> oldTail = tail;
            Node<Task> newNode = new Node<>(task);
            newNode.setPrev(oldTail);
            tail = newNode;
            if (oldTail == null) {
                head = newNode;
            } else {
                oldTail.setNext(newNode);
            }
            nodeMap.put(tail.getData().getId(), tail);
        }

        private void removeNode(Node<Task> removeNode) {
            if (removeNode.getNext() == null && removeNode.getPrev() == null) {
                head = null;
                tail = null;
            } else if (removeNode.getPrev() == null) {
                head = removeNode.getNext();
                head.setPrev(null);
            } else if (removeNode.getNext() == null) {
                tail = removeNode.getPrev();
                tail.setNext(null);
            } else {
                removeNode.getPrev().setNext(removeNode.getNext());
                removeNode.getNext().setPrev(removeNode.getPrev());
            }
        }
    }
}
