/*
 * Copyright 2012 Andrew E. Bruno <aebruno2@buffalo.edu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package edu.buffalo.fusim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Class to encapsulate a Transcript Record from a Gene Model
 * 
 * @author Andrew E. Bruno
 * 
 */
public class TranscriptRecord {

    private String transcriptId;
    private String chrom;
    private String strand;
    private int txStart;
    private int txEnd;
    private int cdsStart;
    private int cdsEnd;
    private int exonCount;
    private int exonBases;
    private int[] exonStarts;
    private int[] exonEnds;
    private int[] exonFrames;
    private String geneId;
    private List<int []> codingExons;

    private TranscriptRecord() {

    }

    public static TranscriptRecord fromGeneModel(String[] fields) {
        if (fields.length != 16)
            throw new RuntimeException(
                    "Invalid RefGene file. records should have 16 fields but found only: "
                            + fields.length);
        
        TranscriptRecord record = new TranscriptRecord();

        record.transcriptId = fields[1];
        record.chrom = fields[2];
        record.strand = fields[3];

        try {
            record.txStart = Integer.valueOf(fields[4]);
            record.txEnd = Integer.valueOf(fields[5]);
            record.cdsStart = Integer.valueOf(fields[6]);
            record.cdsEnd = Integer.valueOf(fields[7]);
            record.exonCount = Integer.valueOf(fields[8]);
            record.exonStarts = TranscriptRecord.toIntArray(fields[9]);
            record.exonEnds = TranscriptRecord.toIntArray(fields[10]);
            record.exonFrames = TranscriptRecord.toIntArray(fields[15]);

            //if("-".equals(record.strand)) {
            //    ArrayUtils.reverse(record.exonStarts);
            //    ArrayUtils.reverse(record.exonEnds);
            //    ArrayUtils.reverse(record.exonFrames);
            //}

            record.exonBases = 0;

            for(int i = 0; i < record.exonStarts.length; i++) {
                record.exonBases += record.exonEnds[i]-record.exonStarts[i];
            }
            
            // Compute coding exons
            record.codingExons = new ArrayList<int []>();

            for(int i = 0; i < record.exonStarts.length; i++) {
                int start = record.exonStarts[i];
                int end = record.exonEnds[i];
                
                if(start > record.cdsEnd) continue;
                if(end < record.cdsStart) continue; 

                if(start >= record.cdsStart && end <= record.cdsEnd) {
                    record.codingExons.add(new int[]{start,end});
                } else if(start <= record.cdsStart && record.cdsStart <= end && end <= record.cdsEnd) {
                    record.codingExons.add(new int[]{record.cdsStart,end});
                } else if(start >= record.cdsStart && record.cdsStart <= end && end >= record.cdsEnd) {
                    record.codingExons.add(new int[]{start,record.cdsEnd});
                } else if(start < record.cdsStart && end > record.cdsEnd) {
                    record.codingExons.add(new int[]{record.cdsStart, record.cdsEnd});
                }
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Invalid RefGene file. Can't parse integer value: ", e);
        }

        record.geneId = fields[12];
        
        return record;
    }

    public List<int []> getCodingExons() {
        return this.codingExons;
    }
    
    public int[] generateExonBreak(boolean keepFirstHalf) {
        if(codingExons.size() == 0) {
            throw new RuntimeException("Missing coding exons: \n"+this.toString());
        }
        
        Random r = new Random();
        int breakIndex = r.nextInt(codingExons.size());
        
        int start = 0;
        int end = breakIndex;
        
        if(!keepFirstHalf) {
            start = breakIndex;
            end = codingExons.size()-1;
        }
        
        int[] exonIndicies = new int[(end-start)+1];
        
        int j = 0;
        for(int i = start; i <= end; i++) {
            exonIndicies[j++] = "-".equals(strand) ? ((codingExons.size()-1)-i) : i;
        }
        
        if("-".equals(strand)) ArrayUtils.reverse(exonIndicies);
        
        return exonIndicies;
    }
    
    private static int[] toIntArray(String str) throws NumberFormatException {
        str = StringUtils.stripEnd(str, ",");
        String[] vals = str.split(",");
        int[] numbers = new int[vals.length];
        
        for(int i = 0; i < vals.length; i++) {
            numbers[i] = Integer.valueOf(vals[i]);
        }
        return numbers;
    }
    
    public String toString() {
        String str = "[\n"; 
        str += transcriptId+": "+chrom + ':' + txStart + '-' + txEnd + " " + strand + "\n";
        str += "Gene: "+geneId+"\n";
        str += "CDS: " +cdsStart+'-'+cdsEnd + "\n";
        str += "Exon Count: " + exonCount + "\n";
        str += "Exon Starts: "+ ArrayUtils.toString(exonStarts) + "\n";
        str += "Exon Ends: "+ArrayUtils.toString(exonEnds) + "\n";
        str += "Exon Frames: "+ArrayUtils.toString(exonFrames) + "\n";
        str += "Coding Exons: ";
        for(int[] x: this.getCodingExons()) {
            str += x[0]+"-"+x[1]+",";
        }
       
        str += "\n]\n";
        
        return str;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public String getChrom() {
        return chrom;
    }

    public String getStrand() {
        return strand;
    }

    public int getTxStart() {
        return txStart;
    }

    public int getTxEnd() {
        return txEnd;
    }

    public int getCdsStart() {
        return cdsStart;
    }

    public int getCdsEnd() {
        return cdsEnd;
    }

    public int getExonCount() {
        return exonCount;
    }

    public int getExonBases() {
        return exonBases;
    }

    public int[] getExonStarts() {
        return exonStarts;
    }

    public int[] getExonEnds() {
        return exonEnds;
    }

    public int[] getExonFrames() {
        return exonFrames;
    }

    public String getGeneId() {
        return geneId;
    }

}
