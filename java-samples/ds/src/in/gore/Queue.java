package in.gore;



/**
*
* @author mgore
* Uses a size element to track the number of elements in the queue.
* removePos points to the head of the queue to remove elements from.
* The isFull and isEmpty rely on the size to return appropriate status.
* When
*/

public class Queue {
    static final int maxSize = 3;
    Object[] data = new Object[maxSize];

    int size = 0;
    int removePos = 0;

    public Queue() {
        
    }

    public void enqueue(Object obj) throws Exception {
        if (size == maxSize)
            throw new Exception("Queue is full");

        // find the next insert location in the array.
        // the removePos+size tells us the array index upto which elements are filled.
        // The modulo operation tells us the next free element.
        int insertPos = (removePos + size)%maxSize;
        data[insertPos] = obj;
        size++;
    }



    public Object dequeue() throws Exception {
        if (size == 0)
            throw new Exception("Queue is empty");
        Object obj = data[removePos];
        data[removePos] = null;
        // The modulo operation helps us wrap around the array.
        removePos = (removePos +1) % maxSize;
        size--;
        return obj;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == maxSize;
    }


    public static void main(String[] args) {
        try {
            Queue q = new Queue();
            q.enqueue(new Object());
            q.enqueue(new Object());
            q.enqueue(new Object());
            //q.dequeue();
            //q.dequeue();
            q.dequeue();
            q.enqueue(new Object());
            q.enqueue(new Object());

        } catch (Exception exp) {
            System.out.println(exp.getMessage());
        }

    }

}
