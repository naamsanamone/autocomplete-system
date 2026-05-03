package com.autocomplete.datastructure;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A generic Min-Heap implementation built from scratch.
 *
 * <p>This is a custom binary heap backed by a dynamic array, NOT using
 * {@link java.util.PriorityQueue}. Hand-implementing this data structure
 * is critical for interviews — it demonstrates deep understanding of:
 * <ul>
 *   <li>Array-based tree representation (parent = i/2, children = 2i, 2i+1)</li>
 *   <li>Heapify-up (sift-up) for insertions</li>
 *   <li>Heapify-down (sift-down) for extractions</li>
 *   <li>Dynamic array resizing</li>
 * </ul>
 *
 * <h3>Complexity</h3>
 * <table>
 *   <tr><td>insert</td><td>O(log N)</td></tr>
 *   <tr><td>extractMin</td><td>O(log N)</td></tr>
 *   <tr><td>peek</td><td>O(1)</td></tr>
 *   <tr><td>size</td><td>O(1)</td></tr>
 * </table>
 *
 * <h3>Why Min-Heap for Top-K?</h3>
 * <p>To find the Top-K largest elements from N candidates:
 * <ul>
 *   <li>Maintain a Min-Heap of size K</li>
 *   <li>For each candidate: if heap.size &lt; K → insert; else if candidate &gt; heap.peek → extractMin + insert</li>
 *   <li>The heap always contains the K largest seen so far</li>
 *   <li><b>Time: O(N log K)</b> vs O(N log N) for sorting all — huge win when K &lt;&lt; N</li>
 * </ul>
 *
 * @param <T> the type of elements, must be comparable or use a custom comparator
 * @author Sai Venkat
 */
public class MinHeap<T> {

    private static final int DEFAULT_CAPACITY = 16;

    private Object[] heap;
    private int size;
    private final Comparator<T> comparator;

    /**
     * Creates a Min-Heap with natural ordering.
     * Elements must implement {@link Comparable}.
     */
    public MinHeap() {
        this(null, DEFAULT_CAPACITY);
    }

    /**
     * Creates a Min-Heap with the given comparator.
     *
     * @param comparator the comparator to determine element ordering
     */
    public MinHeap(Comparator<T> comparator) {
        this(comparator, DEFAULT_CAPACITY);
    }

    /**
     * Creates a Min-Heap with the given comparator and initial capacity.
     *
     * @param comparator      the comparator (null for natural ordering)
     * @param initialCapacity the initial array capacity
     */
    public MinHeap(Comparator<T> comparator, int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial capacity must be >= 1");
        }
        this.comparator = comparator;
        this.heap = new Object[initialCapacity];
        this.size = 0;
    }

    // ═══════════════════════════════════════════════
    //  CORE OPERATIONS
    // ═══════════════════════════════════════════════

    /**
     * Inserts an element into the heap.
     *
     * <p><b>Time:</b> O(log N) — element is placed at the end and
     * bubbled up (heapify-up) to restore the heap property.</p>
     *
     * @param element the element to insert
     * @throws IllegalArgumentException if element is null
     */
    public void insert(T element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot insert null element");
        }

        ensureCapacity();
        heap[size] = element;
        heapifyUp(size);
        size++;
    }

    /**
     * Removes and returns the minimum element from the heap.
     *
     * <p><b>Time:</b> O(log N) — the root is replaced with the last element,
     * which is then sifted down (heapify-down) to restore the heap property.</p>
     *
     * @return the minimum element
     * @throws NoSuchElementException if the heap is empty
     */
    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (size == 0) {
            throw new NoSuchElementException("Heap is empty");
        }

        T min = (T) heap[0];

        // Move last element to root
        size--;
        heap[0] = heap[size];
        heap[size] = null; // help GC

        // Restore heap property
        if (size > 0) {
            heapifyDown(0);
        }

        return min;
    }

    /**
     * Returns the minimum element without removing it.
     *
     * <p><b>Time:</b> O(1)</p>
     *
     * @return the minimum element
     * @throws NoSuchElementException if the heap is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("Heap is empty");
        }
        return (T) heap[0];
    }

    /**
     * Returns the current number of elements in the heap.
     *
     * <p><b>Time:</b> O(1)</p>
     *
     * @return the heap size
     */
    public int size() {
        return size;
    }

    /**
     * Checks if the heap is empty.
     *
     * @return true if the heap contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from the heap.
     */
    public void clear() {
        Arrays.fill(heap, 0, size, null);
        size = 0;
    }

    // ═══════════════════════════════════════════════
    //  HEAPIFY OPERATIONS (The Interview Core)
    // ═══════════════════════════════════════════════

    /**
     * Restores the heap property by bubbling an element UP.
     *
     * <p>Called after insertion. The new element at position {@code index}
     * is compared with its parent. If it's smaller, they swap, and we
     * continue up the tree until the property is restored.</p>
     *
     * <pre>
     *          10          10          5
     *         /  \   →    /  \   →   / \
     *        15   20    15    5     15  10
     *       /          /           /
     *      5          20          20
     *
     *   Insert 5 → place at end → bubble up twice
     * </pre>
     *
     * @param index the index of the element to bubble up
     */
    private void heapifyUp(int index) {
        while (index > 0) {
            int parentIndex = getParentIndex(index);

            if (compare(index, parentIndex) < 0) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break; // Heap property satisfied
            }
        }
    }

    /**
     * Restores the heap property by sifting an element DOWN.
     *
     * <p>Called after extraction. The element at position {@code index}
     * is compared with its children. If it's larger than the smaller
     * child, they swap, and we continue down the tree.</p>
     *
     * <pre>
     *          20          5           5
     *         /  \   →   / \    →    / \
     *        5   10     20  10      7  10
     *       / \        / \         / \
     *      7  15      7  15      20  15
     *
     *   Extract min → move 20 to root → sift down
     * </pre>
     *
     * @param index the index of the element to sift down
     */
    private void heapifyDown(int index) {
        while (true) {
            int smallest = index;
            int leftChild = getLeftChildIndex(index);
            int rightChild = getRightChildIndex(index);

            // Compare with left child
            if (leftChild < size && compare(leftChild, smallest) < 0) {
                smallest = leftChild;
            }

            // Compare with right child
            if (rightChild < size && compare(rightChild, smallest) < 0) {
                smallest = rightChild;
            }

            // If smallest is not the current index, swap and continue
            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break; // Heap property satisfied
            }
        }
    }

    // ═══════════════════════════════════════════════
    //  INDEX CALCULATIONS
    // ═══════════════════════════════════════════════

    /**
     * Parent index in a 0-indexed array: (i - 1) / 2
     */
    private int getParentIndex(int index) {
        return (index - 1) / 2;
    }

    /**
     * Left child index in a 0-indexed array: 2i + 1
     */
    private int getLeftChildIndex(int index) {
        return 2 * index + 1;
    }

    /**
     * Right child index in a 0-indexed array: 2i + 2
     */
    private int getRightChildIndex(int index) {
        return 2 * index + 2;
    }

    // ═══════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════

    /**
     * Compares two elements at the given indices.
     *
     * @return negative if heap[i] < heap[j], positive if heap[i] > heap[j], 0 if equal
     */
    @SuppressWarnings("unchecked")
    private int compare(int i, int j) {
        T a = (T) heap[i];
        T b = (T) heap[j];

        if (comparator != null) {
            return comparator.compare(a, b);
        }

        // Natural ordering (elements must be Comparable)
        return ((Comparable<T>) a).compareTo(b);
    }

    /**
     * Swaps two elements in the array.
     */
    private void swap(int i, int j) {
        Object temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    /**
     * Doubles the array capacity when full.
     */
    private void ensureCapacity() {
        if (size == heap.length) {
            heap = Arrays.copyOf(heap, heap.length * 2);
        }
    }

    /**
     * Validates the heap property (for testing).
     *
     * @return true if the min-heap property holds for all nodes
     */
    public boolean isValid() {
        for (int i = 0; i < size; i++) {
            int left = getLeftChildIndex(i);
            int right = getRightChildIndex(i);

            if (left < size && compare(i, left) > 0) {
                return false;
            }
            if (right < size && compare(i, right) > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MinHeap[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(heap[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
