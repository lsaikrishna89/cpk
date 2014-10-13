/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cpk;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryAccess;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.testUtils.CpkEnvironmentForTesting;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;
import pt.webdetails.cpk.testUtils.PluginUtilsForTesting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class CpkCoreKettleElementTest {

  private static String userDir = System.getProperty( "user.dir" );
  private static CpkCoreService cpkCore;

  @BeforeClass
  public static void setUp() throws IOException, InitializationException, KettleException {

    IRepositoryAccess repAccess = new VfsRepositoryAccess( userDir + "/test-resources/cpkSol",
      userDir + "/test-resources/settings" );
    IPluginUtils pluginUtils = new PluginUtilsForTesting();
    ICpkEnvironment environment = new CpkEnvironmentForTesting( pluginUtils, repAccess );

    KettleEnvironment.init();
    cpkCore = new CpkCoreService( environment );
  }

  @Test
  public void testCpkParameters() throws Exception {
    OutputStream outResponse = new ByteArrayOutputStream();
    cpkCore.createContent( testParameters( outResponse ) );
    JSONObject json = new JSONObject( outResponse.toString() );
    outResponse.close();

    // assert there is a result
    boolean hasResults = json.getJSONObject( "queryInfo" ).length() > 0;
    Assert.assertTrue( hasResults );

    // define the expected result
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put( "pluginId", "cpkSol" );
    parameters.put( "solutionSystemDir", userDir + "/test-resources/" );
    parameters.put( "pluginDir", userDir + "/test-resources/cpkSol/" );
    parameters.put( "pluginSystemDir", userDir + "/test-resources/cpkSol/system/" );
    parameters.put( "webappDir", "" );
    parameters.put( "sessionUsername", "userName" );
    parameters.put( "sessionRoles", "{\"roles\":[\"administrator\",\"authenticated\"]}" );

    // assert that the result is the expected result
    int row = 0;
    for ( int column = 0; column < json.getJSONArray( "metadata" ).length(); column++ ) {
      final String paramName = json.getJSONArray( "metadata" ).getJSONObject( column ).getString( "colName" );
      final String paramValue = json.getJSONArray( "resultset" ).getJSONArray( row ).getString( column );
      if ( parameters.containsKey( paramName ) ) {
        Assert.assertEquals( paramValue, parameters.get( paramName ) );
      }
    }
  }

  private Map<String, Map<String, Object>> testParameters( OutputStream outResponse ) {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> pathMap = new HashMap<String, Object>();
    Map<String, Object> requestMap = new HashMap<String, Object>();

    pathMap.put( "path", "/testParameters" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    requestMap.put( "kettleOutput", "Json" );

    mainMap.put( "path", pathMap );
    mainMap.put( "request", requestMap );
    return mainMap;
  }
}
