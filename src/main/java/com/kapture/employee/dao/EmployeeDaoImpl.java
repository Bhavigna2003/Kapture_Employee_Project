package com.kapture.employee.dao;

import com.kapture.employee.entity.Employee;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class EmployeeDaoImpl implements EmployeeDao {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;


    public void saveEmployee(Employee employee) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(employee);
            transaction.commit();
            logger.info("Employee created successfully with ID: " + employee.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            logger.error("Error creating employee: " + e.getMessage());
        }
    }


    public void saveBulkEmployees(List<Employee> employees) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

//            int batchSize = 50; // Batch size for bulk insert
//            int count = 0;
            for (Employee employee : employees) {
                session.save(employee);
//                count++;
//                if (count % batchSize == 0) {
//                    session.flush();
//                    session.clear();
//                }
            }
            transaction.commit();
        } catch(Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving bulk employees", e);
            throw new RuntimeException("Error saving bulk employees", e);
        } finally {
            if (session != null ) {
                session.close();
            }
        }
    }

    public List<Employee> findByClientId(Long clientId, int page, int size) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Employee> query = session.createQuery("FROM Employee WHERE clientId = :clientId", Employee.class);
            query.setParameter("clientId", clientId);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            List<Employee> resultList = query.list();
            if (resultList != null && !resultList.isEmpty()) {
                return resultList;
            }
            return null;
        } catch (Exception e) {
            logger.error("Error finding employees by client ID", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return null;
    }


    public Page<Employee> findByDesignation(String designation, Pageable pageable) {
        try (Session session = sessionFactory.openSession()) {
            Query<Employee> query = session.createQuery("FROM Employee WHERE designation = :designation", Employee.class);
            query.setParameter("designation", designation);
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
            List<Employee> resultList = query.list();
            Long total = getTotalCountByDesignation(designation);
            return new PageImpl<>(resultList, pageable, total);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in finding employees by designation: " + e.getMessage());
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
    }

    private Long getTotalCountByDesignation(String designation) {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> countQuery = session.createQuery("SELECT COUNT(*) FROM Employee WHERE designation = :designation", Long.class);
            countQuery.setParameter("designation", designation);
            return countQuery.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error fetching total count by designation: " + e.getMessage());
            return 0L;
        }
    }


    public List<Employee> findAllEmployees() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Employee", Employee.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error finding all employees: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<Employee> findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Employee.class, id));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error finding employee by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Employee> findByEmpCode(String empCode) {
        try (Session session = sessionFactory.openSession()) {
            Query<Employee> query = session.createQuery("FROM Employee WHERE empCode = :empCode", Employee.class);
            query.setParameter("empCode", empCode);
            return Optional.ofNullable(query.uniqueResult());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error finding employee by empCode: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void updateEmployee(Employee employee) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.update(employee);
            transaction.commit();
            logger.info("Employee updated successfully with ID: " + employee.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            logger.error("Error updating employee: " + e.getMessage());
        }
    }

    public void deleteEmployee(Employee employee) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.delete(employee);
            transaction.commit();
            logger.info("Employee deleted successfully with ID: " + employee.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
            logger.error("Error deleting employee: " + e.getMessage());
        }
    }
}
