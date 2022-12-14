package com.bookStore.goods.book.dao;

import com.bookStore.goods.Utils.CommonUtils;
import com.bookStore.goods.Utils.MyQueryRunner;
import com.bookStore.goods.book.domain.Book;
import com.bookStore.goods.category.domain.Category;
import com.bookStore.goods.Utils.Expression;
import com.bookStore.goods.Utils.PageBean;
import com.bookStore.goods.Utils.PageConstants;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookDao {

	private QueryRunner qr = new MyQueryRunner();

	public void add(Book book) throws SQLException {
		String sql = "insert into book(bid,bname,author,price,currPrice," +
				"discount,press,publishtime,edition,pageNum,wordNum,printtime," +
				"booksize,paper,cid,image_w,image_b)" +
				" values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] params = {book.getBid(),book.getBname(),book.getAuthor(),
				book.getPrice(),book.getCurrPrice(),book.getDiscount(),
				book.getPress(),book.getPublishtime(),book.getEdition(),
				book.getPageNum(),book.getWordNum(),book.getPrinttime(),
				book.getBooksize(),book.getPaper(), book.getCategory().getCid(),
				book.getImage_w(),book.getImage_b()};
		qr.update(sql, params);
	}

	public void delete(String bid) throws SQLException {
		String sql = "delete from book where bid=?";
		qr.update(sql, bid);
	}

	public void edit(Book book) throws SQLException {
		String sql = "update book set bname=?,author=?,price=?,currPrice=?," +
				"discount=?,press=?,publishtime=?,edition=?,pageNum=?,wordNum=?," +
				"printtime=?,booksize=?,paper=?,cid=? where bid=?";
		Object[] params = {book.getBname(),book.getAuthor(),
				book.getPrice(),book.getCurrPrice(),book.getDiscount(),
				book.getPress(),book.getPublishtime(),book.getEdition(),
				book.getPageNum(),book.getWordNum(),book.getPrinttime(),
				book.getBooksize(),book.getPaper(), 
				book.getCategory().getCid(),book.getBid()};
		qr.update(sql, params);
	}

	public Book findByBid(String bid) throws SQLException {
		String sql = "SELECT * FROM book b, category c WHERE b.cid=c.cid AND b.bid=?";
		// ????????????????????????????????????book????????????????????????cid??????
		Map<String,Object> map = qr.query(sql, new MapHandler(), bid);
		// ???Map?????????cid??????????????????????????????Book?????????
		Book book = CommonUtils.toBean(map, Book.class);
		// ???Map???cid???????????????Category???????????????Category??????cid
		Category category = CommonUtils.toBean(map, Category.class);
		// ??????????????????
		book.setCategory(category);
		
		// ???pid???????????????????????????Category parnet??????pid????????????????????????parent??????category
		if(map.get("pid") != null) {
			Category parent = new Category();
			parent.setCid((String)map.get("pid"));
			category.setParent(parent);
		}
		return book;
	}
	
	//?????????????????????????????????
	public int findBookCountByCategory(String cid) throws SQLException {
		String sql = "select count(*) from book where cid=?";
		Number cnt = (Number)qr.query(sql, new ScalarHandler(), cid);
		return cnt == null ? 0 : cnt.intValue();
	}
	
	//???????????????
	public PageBean<Book> findByCategory(String cid, int pc) throws SQLException {
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("cid", "=", cid));
		return findByCriteria(exprList, pc);
	}
	
	// ?????????????????????
	public PageBean<Book> findByBname(String bname, int pc) throws SQLException {
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("bname", "like", "%" + bname + "%"));
		return findByCriteria(exprList, pc);
	}
	
	//????????????
	public PageBean<Book> findByAuthor(String author, int pc) throws SQLException {
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("author", "like", "%" + author + "%"));
		return findByCriteria(exprList, pc);
	}
	
	//???????????????
	public PageBean<Book> findByPress(String press, int pc) throws SQLException {
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("press", "like", "%" + press + "%"));
		return findByCriteria(exprList, pc);
	}
	
	// ?????????????????????
	public PageBean<Book> findByCombination(Book criteria, int pc) throws SQLException {
		List<Expression> exprList = new ArrayList<Expression>();
		exprList.add(new Expression("bname", "like", "%" + criteria.getBname() + "%"));
		exprList.add(new Expression("author", "like", "%" + criteria.getAuthor() + "%"));
		exprList.add(new Expression("press", "like", "%" + criteria.getPress() + "%"));
		return findByCriteria(exprList, pc);
	}
	
	//?????????????????????
	private PageBean<Book> findByCriteria(List<Expression> exprList, int pc) throws SQLException {
		int ps = PageConstants.BOOK_PAGE_SIZE;//???????????????
		StringBuilder whereSql = new StringBuilder(" where 1=1"); 
		List<Object> params = new ArrayList<Object>();//SQL???????????????????????????????????????
		for(Expression expr : exprList) {
			whereSql.append(" and ").append(expr.getName())
				.append(" ").append(expr.getOperator()).append(" ");
			if(!expr.getOperator().equals("is null")) {
				whereSql.append("?");
				params.add(expr.getValue());
			}
		}
		String sql = "select count(*) from book" + whereSql;
		Number number = (Number)qr.query(sql, new ScalarHandler(), params.toArray());
		int tr = number.intValue();//?????????????????????
		sql = "select * from book" + whereSql + " order by orderBy limit ?,?";
		params.add((pc-1) * ps);//??????????????????????????????
		params.add(ps);//??????????????????????????????????????????
		List<Book> beanList = qr.query(sql, new BeanListHandler<Book>(Book.class),
				params.toArray());
		PageBean<Book> pb = new PageBean<Book>();
		pb.setBeanList(beanList);
		pb.setPc(pc);
		pb.setPs(ps);
		pb.setTr(tr);
		return pb;
	}

	// ???????????????????????????
	public List<Book> bookRankingList() throws SQLException {
		String sql = "SELECT * from book where bid in (SELECT s.bid FROM `order` p,orderitem s where " +
				"p.oid = s.oid AND p.`status` = 4 GROUP BY bid order by SUM(s.quantity) desc ) LIMIT 6";
		List<Book> bookList = qr.query(sql, new BeanListHandler<Book>(Book.class));
		return bookList;
	}
}
