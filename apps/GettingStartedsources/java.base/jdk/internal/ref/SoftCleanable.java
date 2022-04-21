/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.internal.ref;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Objects;

/**
 * SoftCleanable subclasses efficiently encapsulate cleanup state and
 * the cleaning action.
 * Subclasses implement the abstract {@link #performCleanup()}  method
 * to provide the cleaning action.
 * When constructed, the object reference and the {@link Cleaner.Cleanable Cleanable}
 * are registered with the {@link Cleaner}.
 * The Cleaner invokes {@link Cleaner.Cleanable#clean() clean} after the
 * referent becomes softly reachable.
 */
public abstract class SoftCleanable<T> extends SoftReference<T>
        implements Cleaner.Cleanable {

    /**
     * Links to previous and next in a doubly-linked list.
     */
    SoftCleanable<?> prev = this, next = this;

    /**
     * The list of SoftCleanable; synchronizes insert and remove.
     */
    private final SoftCleanable<?> list;

    /**
     * Constructs new {@code SoftCleanableReference} with
     * {@code non-null referent} and {@code non-null cleaner}.
     * The {@code cleaner} is not retained by this reference; it is only used
     * to register the newly constructed {@link Cleaner.Cleanable Cleanable}.
     *
     * @param referent the referent to track
     * @param cleaner  the {@code Cleaner} to register with
     */
    public SoftCleanable(T referent, Cleaner cleaner) {
        super(Objects.requireNonNull(referent), CleanerImpl.getCleanerImpl(cleaner).queue);
        list = CleanerImpl.getCleanerImpl(cleaner).softCleanableList;
        insert();

        // Ensure referent and cleaner remain accessible
        Reference.reachabilityFence(referent);
        Reference.reachabilityFence(cleaner);
    }

    /**
     * Construct a new root of the list; not inserted.
     */
    SoftCleanable() {
        super(null, null);
        this.list = this;
    }

    /**
     * Insert this SoftCleanableReference after the list head.
     */
    private void insert() {
        synchronized (list) {
            prev = list;
            next = list.next;
            next.prev = this;
            list.next = this;
        }
    }

    /**
     * Remove this SoftCleanableReference from the list.
     *
     * @return true if Cleanable was removed or false if not because
     * it had already been removed before
     */
    private boolean remove() {
        synchronized (list) {
            if (next != this) {
                next.prev = prev;
                prev.next = next;
                prev = this;
                next = this;
                return true;
            }
            return false;
        }
    }

    /**
     * Returns true if the list's next reference refers to itself.
     *
     * @return true if the list is empty
     */
    boolean isListEmpty() {
        synchronized (list) {
            return list == list.next;
        }
    }

    /**
     * Unregister this SoftCleanable reference and invoke {@link #performCleanup()},
     * ensuring at-most-once semantics.
     */
    @Override
    public final void clean() {
        if (remove()) {
            super.clear();
            performCleanup();
        }
    }

    /**
     * Unregister this SoftCleanable and clear the reference.
     * Due to inherent concurrency, {@link #performCleanup()} may still be invoked.
     */
    @Override
    public void clear() {
        if (remove()) {
            super.clear();
        }
    }

    /**
     * The {@code performCleanup} abstract method is overridden
     * to implement the cleaning logic.
     * The {@code performCleanup} method should not be called except
     * by the {@link #clean} method which ensures at most once semantics.
     */
    protected abstract void performCleanup();

    /**
     * This method always throws {@link UnsupportedOperationException}.
     * Enqueuing details of {@link Cleaner.Cleanable}
     * are a private implementation detail.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public final boolean isEnqueued() {
        throw new UnsupportedOperationException("isEnqueued");
    }

    /**
     * This method always throws {@link UnsupportedOperationException}.
     * Enqueuing details of {@link Cleaner.Cleanable}
     * are a private implementation detail.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public final boolean enqueue() {
        throw new UnsupportedOperationException("enqueue");
    }
}
