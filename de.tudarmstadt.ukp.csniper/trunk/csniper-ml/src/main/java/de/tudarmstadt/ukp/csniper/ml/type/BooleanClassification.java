/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/* First created by JCasGen Mon May 07 13:31:09 CEST 2012 */
package de.tudarmstadt.ukp.csniper.ml.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** This type is intended to be used with a binary classifier.
It contains fields for
- gold value
- predicted value
- score used for prediction
- category which is being predicted
 * Updated by JCasGen Sat Apr 20 16:04:12 CEST 2013
 * XML source: D:/workspace/de.tudarmstadt.ukp.csniper/csniper-ml/src/main/resources/desc/type/BooleanClassification.xml
 * @generated */
public class BooleanClassification extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(BooleanClassification.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected BooleanClassification() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public BooleanClassification(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public BooleanClassification(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public BooleanClassification(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: expectedLabel

  /** getter for expectedLabel - gets 
   * @generated */
  public boolean getExpectedLabel() {
    if (BooleanClassification_Type.featOkTst && ((BooleanClassification_Type)jcasType).casFeat_expectedLabel == null)
      jcasType.jcas.throwFeatMissing("expectedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((BooleanClassification_Type)jcasType).casFeatCode_expectedLabel);}
    
  /** setter for expectedLabel - sets  
   * @generated */
  public void setExpectedLabel(boolean v) {
    if (BooleanClassification_Type.featOkTst && ((BooleanClassification_Type)jcasType).casFeat_expectedLabel == null)
      jcasType.jcas.throwFeatMissing("expectedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((BooleanClassification_Type)jcasType).casFeatCode_expectedLabel, v);}    
   
    
  //*--------------*
  //* Feature: predictedLabel

  /** getter for predictedLabel - gets 
   * @generated */
  public boolean getPredictedLabel() {
    if (BooleanClassification_Type.featOkTst && ((BooleanClassification_Type)jcasType).casFeat_predictedLabel == null)
      jcasType.jcas.throwFeatMissing("predictedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((BooleanClassification_Type)jcasType).casFeatCode_predictedLabel);}
    
  /** setter for predictedLabel - sets  
   * @generated */
  public void setPredictedLabel(boolean v) {
    if (BooleanClassification_Type.featOkTst && ((BooleanClassification_Type)jcasType).casFeat_predictedLabel == null)
      jcasType.jcas.throwFeatMissing("predictedLabel", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((BooleanClassification_Type)jcasType).casFeatCode_predictedLabel, v);}    
   
    
  //*--------------*
  //* Feature: Score

  /** getter for Score - gets 
   * @generated */
  public double getScore() {
    if (BooleanClassification_Type.featOkTst && ((BooleanClassification_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((BooleanClassification_Type)jcasType).casFeatCode_score);}
    
  /** setter for Score - sets  
   * @generated */
  public void setScore(double v) {
    if (BooleanClassification_Type.featOkTst && ((BooleanClassification_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((BooleanClassification_Type)jcasType).casFeatCode_score, v);}    
  }

    
