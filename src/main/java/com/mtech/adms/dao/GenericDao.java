package com.mtech.adms.dao;

import java.util.List;
import java.util.Optional;

/**
 * Standard CRUD contract that every entity-specific DAO implements.
 * T is the model type (e.g. Asset), ID is the primary key type (typically Integer).
 *
 * Concrete DAOs (AssetDao, EmployeeDao, etc.) implement this interface
 * using PreparedStatement against the corresponding table. Keeping the
 * contract consistent means the Service layer always knows what
 * operations are available, regardless of which entity it's working with.
 */
public interface GenericDao<T, ID> {

    /**
     * Inserts a new record and returns the generated model
     * (typically with the auto-generated ID populated).
     */
    T insert(T entity);

    /**
     * Updates an existing record. Returns true if a row was affected.
     */
    boolean update(T entity);

    /**
     * Finds a single record by its primary key.
     * Returns Optional.empty() if no match - callers decide whether
     * that's an error (throw RecordNotFoundException) or acceptable.
     */
    Optional<T> findById(ID id);

    /**
     * Returns all records. For large tables, feature-specific DAOs
     * may add paginated/filtered variants alongside this.
     */
    List<T> findAll();

    /**
     * Deletes a record by primary key. Returns true if a row was affected.
     * Note: per our soft-delete design, most entities won't actually
     * call this - they'll use an update() that flips is_active instead.
     */
    boolean deleteById(ID id);
}