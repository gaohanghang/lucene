package com.how2java;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class TestLucene {

	public static void main(String[] args) throws Exception {
		// 1. 准备中文分词器
		IKAnalyzer analyzer = new IKAnalyzer();

		// 2. 索引
		List<String> productNames = new ArrayList<>();
		productNames.add("飞利浦led灯泡e27螺口暖白球泡灯家用照明超亮节能灯泡转色温灯泡");
		productNames.add("飞利浦led灯泡e14螺口蜡烛灯泡3W尖泡拉尾节能灯泡暖黄光源Lamp");
		productNames.add("雷士照明 LED灯泡 e27大螺口节能灯3W球泡灯 Lamp led节能灯泡");
		productNames.add("飞利浦 led灯泡 e27螺口家用3w暖白球泡灯节能灯5W灯泡LED单灯7w");
		productNames.add("飞利浦led小球泡e14螺口4.5w透明款led节能灯泡照明光源lamp单灯");
		productNames.add("飞利浦蒲公英护眼台灯工作学习阅读节能灯具30508带光源");
		productNames.add("欧普照明led灯泡蜡烛节能灯泡e14螺口球泡灯超亮照明单灯光源");
		productNames.add("欧普照明led灯泡节能灯泡超亮光源e14e27螺旋螺口小球泡暖黄家用");
		productNames.add("聚欧普照明led灯泡节能灯泡e27螺口球泡家用led照明单灯超亮光源");
		Directory index = createIndex(analyzer, productNames);

		// 3. 查询器
		String keyword = "护眼带光源";
		Query query = new QueryParser("name", analyzer).parse(keyword);


		// 4. 搜索
		IndexReader reader = DirectoryReader.open(index);
		IndexSearcher searcher = new IndexSearcher(reader);
		int numberPerPage = 1000;
		System.out.printf("当前一共有%d条数据%n",productNames.size());
		System.out.printf("查询关键字是：\"%s\"%n",keyword);
		ScoreDoc[] hits = searcher.search(query, numberPerPage).scoreDocs;

		// 5. 显示查询结果
		showSearchResults(searcher, hits, query, analyzer);
		// 6. 关闭查询
		reader.close();
	}

	private static void showSearchResults(IndexSearcher searcher, ScoreDoc[] hits, Query query, IKAnalyzer analyzer)
			throws Exception {
		System.out.println("找到 " + hits.length + " 个命中.");
		System.out.println("序号\t匹配度得分\t结果");
		for (int i = 0; i < hits.length; ++i) {
			//然后获取当前结果的docid, 这个docid相当于就是这个数据在索引中的主键
			ScoreDoc scoreDoc= hits[i];
			int docId = scoreDoc.doc;
			//再根据主键docid，通过搜索器从索引里把对应的Document取出来
			Document d = searcher.doc(docId);
			/*
				接着就打印出这个Document里面的数据。 虽然当前Document只有name一个字段，但是代码还是通过遍历所有字段的形式，
				打印出里面的值，这样当Docment有多个字段的时候，代码就不用修改了，兼容性更好点。
				scoreDoc.score 表示当前命中的匹配度得分，越高表示匹配程度越高
			 */
			List<IndexableField> fields = d.getFields();
			System.out.print((i + 1));
			System.out.print("\t" + scoreDoc.score);
			for (IndexableField f : fields) {
				System.out.print("\t" + d.get(f.name()));
			}
			System.out.println();
		}
	}

	// 通过createIndex方法，把它加入到索引当中
	private static Directory createIndex(IKAnalyzer analyzer, List<String> products) throws IOException {
		// 创建内存索引
		Directory index = new RAMDirectory();
		// 根据中文分词器创建配置对象
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		// 创建索引 writer
		IndexWriter writer = new IndexWriter(index, config);

		// 遍历那10条数据，把他们挨个放进索引里
		for (String name : products) {
			addDoc(writer, name);
		}
		writer.close();
		return index;

	}

	/*
		每条数据创建一个Document，并把这个Document放进索引里。
		这个Document有一个字段，叫做"name"。 TestLucene.java 第49行创建查询器，
		就会指定查询这个字段
	 */
	private static void addDoc(IndexWriter w, String name) throws IOException {
		Document doc = new Document();
		doc.add(new TextField("name", name, Field.Store.YES));
		w.addDocument(doc);
	}

}
