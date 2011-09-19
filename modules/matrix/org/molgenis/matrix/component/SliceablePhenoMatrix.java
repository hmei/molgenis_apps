package org.molgenis.matrix.component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.matrix.MatrixException;
import org.molgenis.matrix.component.general.MatrixQueryRule;
import org.molgenis.matrix.component.interfaces.BasicMatrix;
import org.molgenis.matrix.component.interfaces.SliceableMatrix;
import org.molgenis.organization.Investigation;
import org.molgenis.pheno.ObservableFeature;
import org.molgenis.pheno.ObservationElement;
import org.molgenis.pheno.ObservationTarget;
import org.molgenis.pheno.ObservedValue;

/**
 * Sliceable version of the PhenoMatrix. This assumes the rows are
 * ObservationTarget, the columns ObservableFeature and there can be zero or
 * more ObservedValue for each combination (hence return List &lt; ObservedValue &gt; for
 * each value 'V')
 * 
 * Slicing will be done by setting filters.
 * 
 * The data is retrieved by (a) retrieving visible columns and rows and (2)
 * retrieval of the matching data using columns and rows as filters. The whole
 * set is filtered by investigation.
 * 
 */
public class SliceablePhenoMatrix<R extends ObservationElement,C extends ObservationElement>
		extends
		AbstractObservationElementMatrix<R, C, List<ObservedValue>>
		implements
		SliceableMatrix<R, C, List<ObservedValue>>
{
	/**
	 * Construct sliceable matrix for one Data set.
	 * 
	 * @param database
	 * @param data
	 */
	public SliceablePhenoMatrix(Database database, Class<R> rowClass, Class<C> colClass)
	{
		this.database = database;
		this.rowClass = rowClass;
		this.colClass = colClass;
		this.valueClass = ObservedValue.class;
	}

	@Override
	public List<R> getRowHeaders() throws MatrixException
	{
		// reload the rowheaders if filters have changed.
		if (rowDirty)
		{
			try
			{
				Query<R> query = this
						.createSelectQuery(getRowClass());
				this.rowHeaders = query.find();
				rowDirty = false;
			}
			catch (Exception e)
			{
				throw new MatrixException(e);
			}
		}
		return rowHeaders;
	}

	public Integer getRowCount() throws MatrixException
	{
		// fire a count query on headers
		try
		{
			return this.createCountQuery(getRowClass()).count();
		}
		catch (DatabaseException e)
		{
			throw new MatrixException(e);
		}
	}

	@Override
	public List<C> getColHeaders() throws MatrixException
	{
		// reload the rowheaders if filters have changed.
		if (colDirty)
		{
			try
			{
				Query<C> query = this
						.createSelectQuery(getColClass());
				this.colHeaders = query.find();
				colDirty = false;
			}
			catch (Exception e)
			{
				throw new MatrixException(e);
			}
		}
		return colHeaders;
	}

	public Integer getColCount() throws MatrixException
	{
		// fire count query on col headers
		try
		{
			return this.createCountQuery(getColClass()).count();
		}
		catch (DatabaseException e)
		{
			throw new MatrixException(e);
		}
	}

	@Override
	public BasicMatrix<R, C, List<ObservedValue>> getResult()
			throws Exception
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() throws MatrixException
	{
		// empty the rules
		this.rules = new ArrayList<MatrixQueryRule>();

		// empty the caches
		colDirty = true;
		colOffset = 0;
		rowDirty = true;
		rowOffset = 0;

	}

	/**
	 * Helper method to create a 'count' query. Difference with a normal query
	 * is that there is no limit/offset on it
	 */
	private <D extends ObservationElement> Query<D> createCountQuery(
			Class<D> xClass) throws MatrixException
	{
		return this.createQuery(xClass, true);
	}

	/** Helper method to produce a selection query for columns or rows */
	private <D extends ObservationElement> Query<D> createSelectQuery(
			Class<D> xClass) throws MatrixException
	{
		return this.createQuery(xClass, false);
	}

	/**
	 * 
	 * @param field
	 *            , either ObservedValue.FEATURE or ObservedValue.TARGET
	 * @throws MatrixException
	 */
	private <D extends ObservationElement> Query<D> createQuery(
			Class<D> xClass, boolean countAll) throws MatrixException
	{
		// If xClass == getRowClass():
		// A. filter on rowIndex + rowHeaderProperty
		// B. filter on colValue: 1 subquery per column
		// C. filter on rowOffset and rowLimit

		try
		{
			// parameterize the refresh of the dim, either TARGET or FEATURE
			String xDim = ObservedValue.TARGET;
			MatrixQueryRule.Type xIndexFilterType = MatrixQueryRule.Type.rowIndex;
			MatrixQueryRule.Type xHeaderFilterType = MatrixQueryRule.Type.rowHeader;
			MatrixQueryRule.Type xValuesFilterType = MatrixQueryRule.Type.colValues;
			MatrixQueryRule.Type xValuePropertyFilterType = MatrixQueryRule.Type.colValueProperty;
			if (xClass.equals(getColClass()))
			{
				xDim = ObservedValue.FEATURE;
				xIndexFilterType = MatrixQueryRule.Type.colIndex;
				xHeaderFilterType = MatrixQueryRule.Type.colHeader;
				xValuesFilterType = MatrixQueryRule.Type.rowValues;
				xValuePropertyFilterType = MatrixQueryRule.Type.rowValueProperty;
			}

			// Impl

			// Impl A: header query
			Query<D> xQuery = database.query(xClass);
			for (MatrixQueryRule rule : rules)
			{
				// rewrite rule(type=rowIndex) to rule(type=rowHeader, field=id)
				if (rule.getFilterType().equals(xIndexFilterType))
				{
					rule.setField(ObservedValue.ID);
					rule.setFilterType(xHeaderFilterType);
				}
				// add rowHeader filters to query / remember sort rules
				if (rule.getFilterType().equals(xHeaderFilterType))
				{
					xQuery.addRules(rule);
				}
				// ignore all other rules
			}

			// select * from Individual where id in (select target from
			// observedvalue where feature = 1 AND value > 10 AND target in
			// (select target from observedvalue ));

			// Impl B: create subquery per column, order matters because of
			// sorting (not supported).
			Map<Integer, Query<ObservedValue>> subQueries = new LinkedHashMap<Integer, Query<ObservedValue>>();
			for (MatrixQueryRule rule : rules)
			{
				// only add colValues / rowValues as subquery
				if (rule.getFilterType().equals(xValuePropertyFilterType))
				{
					// create a new subquery for each colValues column
					if (subQueries.get(rule.getDimIndex()) == null)
					{
						Query<ObservedValue> subQuery = database.query(this
								.getValueClass());
						// filter on data
						// if(data != null)
						// subQuery.eq(TextDataElement.DATA, data.getIdValue());
						// filter on the column/row
						subQuery.eq(xDim, rule.getDimIndex());
						subQueries.put(rule.getDimIndex(), subQuery);
					}
					subQueries.get(rule.getDimIndex()).addRules(rule);
				}
				// ignore all other rules
			}

			// add each subquery as condition on ObservedValue.FEATURE/ObservedValue.TARGET
			for (Query<ObservedValue> q : subQueries.values())
			{
				String sql = q.createFindSql();
				// strip 'select ... from' and replace with 'select id from'
				sql = "SELECT TextDataElement." + xDim + " "
						+ sql.substring(sql.indexOf("FROM"));
				// use QueryRule.Operator.IN_SUBQUERY
				xQuery.subquery(ObservationElement.ID, sql);
			}

			// add limit and offset, unless count
			if (!countAll)
			{
				if (xClass.equals(getColClass()))
				{
					xQuery.limit(colLimit);
					xQuery.offset(colOffset);
				}
				else
				{
					xQuery.limit(rowLimit);
					xQuery.offset(rowOffset);
				}
			}

			return xQuery;
		}
		catch (Exception e)
		{
			throw new MatrixException(e);
		}
	}

	@Override
	public List<ObservedValue>[][] getValues() throws MatrixException
	{
		try
		{
			// get the indices (map to real coordinates)
			final List<Integer> rowIndexes = getRowIndices();
			final List<Integer> colIndexes = getColIndices();

			// create matrix of suitable size
			final List<ObservedValue>[][] valueMatrix = create(getRowLimit(),
					getColLimit(), valueClass);

			// retrieve values matching the selected indexes
			Query<ObservedValue> query = database.query(valueClass);
			query.in(ObservedValue.FEATURE, this.getColIndices());
			query.in(ObservedValue.TARGET, this.getRowIndices());

			// use the streaming interface?
			List<ObservedValue> values = query.find();

			for (ObservedValue value : values)
			{
				if (valueMatrix[rowIndexes.indexOf(value.getTarget())][colIndexes
						.indexOf(value.getFeature())] == null)
				{
					valueMatrix[rowIndexes.indexOf(value.getTarget())][colIndexes
							.indexOf(value.getFeature())] = new ArrayList<ObservedValue>();
				}
				valueMatrix[rowIndexes.indexOf(value.getTarget())][colIndexes
						.indexOf(value.getFeature())].add(value);
			}

			return valueMatrix;
		}
		catch (Exception e)
		{
			throw new MatrixException(e);
		}
	}
}