package org.molgenis.mutation.service;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GffService {
	private Database db;

	//@Autowired
	public void setDatabase(Database db) {
		this.db = db;
	}

	public String exportExons() throws DatabaseException {
		// StringBuilder b = new StringBuilder();
		//
		// List<SequenceCharacteristic> geneList =
		// this.db.query(SequenceCharacteristic.class).equals(SequenceCharacteristic.FEATURETYPE_NAME,
		// "gene").find();
		//
		// if (geneList.size() != 1)
		// return "";
		//
		// SequenceCharacteristic gene = geneList.get(0);
		//
		//
		// Chromosome chromosome = this.db.findById(Chromosome.class,
		// gene.getChromosome_Id());
		//
		// b.append("##gff-version 3\n");
		// b.append(String.format("##sequence-region chr%s 1 199505740\n",
		// chromosome.getName()));
		// //
		// b.append(String.format("chr%s\tsource\tchromosome\t%d\t%d\t.\t%s\t.\tID=chr%s;Name=chr%s\n",
		// // chromosome.getName(),
		// // 1,
		// // 199505740, //chromosome.getBpLength(),
		// // ("F".equals(gene.getOrientation()) ? "+" : "-"),
		// // chromosome.getName(), chromosome.getName()));
		// b.append(String.format("chr%s\t.\tgene\t%d\t%d\t.\t%s\t.\tID=gene%s;Name=%s\n",
		// chromosome.getName(),
		// gene.getEndBP(),
		// gene.getStartBP(),
		// ("F".equals(gene.getOrientation()) ? "+" : "-"),
		// gene.getId(), gene.getName()));
		//
		// b.append(String.format("chr%s\texonerate\tmRNA\t%d\t%d\t.\t%s\t.\tID=mRNA%s;Parent=gene%s\n",
		// chromosome.getName(),
		// gene.getEndBP(),
		// gene.getStartBP(),
		// ("F".equals(gene.getOrientation()) ? "+" : "-"),
		// gene.getId(), gene.getId()));
		//
		// List<Exon> exonList = this.db.query(Exon.class).equals(Exon.GENE,
		// gene.getId()).sortASC(Exon.GDNA_POSITION).find();
		//
		// for (Exon exon : exonList)
		// {
		// b.append(String.format("chr%s\texonerate\t%s\t%d\t%d\t.\t%s\t.\tID=%s;Parent=mRNA%s;Name=%s\n",
		// chromosome.getName(),
		// (exon.getIsIntron() ? (exon.getName().endsWith("UTR") ? "UTR" :
		// "intron") : "exon"),
		// exon.getGdna_Position() - exon.getLength() + 1,
		// exon.getGdna_Position(),
		// ("F".equals(gene.getOrientation()) ? "+" : "-"),
		// exon.getId(), gene.getId(), exon.getName()));
		// }
		//
		// return b.toString();
		return "";
	}
}
