package com.nimbusds.jose.jwk;


import java.util.*;

import com.nimbusds.jose.Algorithm;
import junit.framework.TestCase;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.util.Base64URL;


/**
 * Tests the JWK selector.
 *
 * @author Vladimir Dzhuvinov
 */
public class JWTSelectorTest extends TestCase {


	public void testConstructor() {

		JWKSelector selector = new JWKSelector();

		assertNull(selector.getKeyTypes());
		assertNull(selector.getKeyUses());
		assertNull(selector.getAlgorithms());
		assertNull(selector.getKeyIDs());
		assertFalse(selector.isPrivateOnly());
		assertFalse(selector.isPublicOnly());
	}


	public void testPrivateAndPublicOnlySetters() {

		JWKSelector selector = new JWKSelector();

		assertFalse(selector.isPrivateOnly());
		assertFalse(selector.isPublicOnly());

		selector.setPrivateOnly(true);
		assertTrue(selector.isPrivateOnly());

		selector.setPublicOnly(true);
		assertTrue(selector.isPublicOnly());
	}


	public void testSetSetters() {

		JWKSelector selector = new JWKSelector();

		Set<KeyType> types = new HashSet<KeyType>();
		types.add(KeyType.RSA);
		selector.setKeyTypes(types);
		assertEquals(types, selector.getKeyTypes());

		Set<Use> uses = new HashSet<Use>();
		uses.add(Use.SIGNATURE);
		selector.setKeyUses(uses);
		assertEquals(uses, selector.getKeyUses());

		Set<Algorithm> algs = new HashSet<Algorithm>();
		algs.add(JWSAlgorithm.PS256);
		selector.setAlgorithms(algs);
		assertEquals(algs, selector.getAlgorithms());

		Set<String> ids = new HashSet<String>();
		ids.add("1");
		selector.setKeyIDs(ids);
		assertEquals(ids, selector.getKeyIDs());
	}


	public void testVarArgSetters() {

		JWKSelector selector = new JWKSelector();

		selector.setKeyTypes(KeyType.EC, KeyType.RSA, null);
		Set<KeyType> types = selector.getKeyTypes();
		assertTrue(types.containsAll(Arrays.asList(KeyType.EC, KeyType.RSA, null)));
		assertEquals(3, types.size());

		selector.setKeyUses(Use.SIGNATURE, null);
		Set<Use> uses = selector.getKeyUses();
		assertTrue(uses.containsAll(Arrays.asList(Use.SIGNATURE, null)));
		assertEquals(2, uses.size());

		selector.setAlgorithms(JWSAlgorithm.RS256, JWSAlgorithm.PS256);
		Set<Algorithm> algs = selector.getAlgorithms();
		assertTrue(algs.containsAll(Arrays.asList(JWSAlgorithm.RS256, JWSAlgorithm.PS256)));
		assertEquals(2, algs.size());

		selector.setKeyIDs("1", "2", "3", null);
		Set<String> ids = selector.getKeyIDs();
		assertTrue(ids.containsAll(Arrays.asList("1", "2", "3", null)));
		assertEquals(4, ids.size());
	}


	public void testSelectFromNullSet() {

		List<JWK> matches = new JWKSelector().select(null);

		assertTrue(matches.isEmpty());
	}


	public void testSelectFromEmptySet() {

		List<JWK> matches = new JWKSelector().select(new JWKSet());

		assertTrue(matches.isEmpty());
	}


	public void testMatchType() {

		JWKSelector selector = new JWKSelector();
		selector.setKeyType(KeyType.RSA);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").build());
		keyList.add(new ECKey.Builder(ECKey.Curve.P_256, new Base64URL("x"), new Base64URL("y")).keyID("2").build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals(KeyType.RSA, key1.getKeyType());
		assertEquals("1", key1.getKeyID());

		assertEquals(1, matches.size());
	}


	public void testMatchTwoTypes() {

		JWKSelector selector = new JWKSelector();
		selector.setKeyTypes(KeyType.RSA, KeyType.EC);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").build());
		keyList.add(new ECKey.Builder(ECKey.Curve.P_256, new Base64URL("x"), new Base64URL("y")).keyID("2").build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals(KeyType.RSA, key1.getKeyType());
		assertEquals("1", key1.getKeyID());

		ECKey key2 = (ECKey)matches.get(1);
		assertEquals(KeyType.EC, key2.getKeyType());
		assertEquals("2", key2.getKeyID());

		assertEquals(2, matches.size());
	}


	public void testMatchUse() {

		JWKSelector selector = new JWKSelector();
		selector.setKeyUse(Use.ENCRYPTION);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").keyUse(Use.ENCRYPTION).build());
		keyList.add(new ECKey.Builder(ECKey.Curve.P_256, new Base64URL("x"), new Base64URL("y")).keyID("2").build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals(KeyType.RSA, key1.getKeyType());
		assertEquals(Use.ENCRYPTION, key1.getKeyUse());
		assertEquals("1", key1.getKeyID());

		assertEquals(1, matches.size());
	}


	public void testMatchUseNotSpecifiedOrSignature() {

		JWKSelector selector = new JWKSelector();
		selector.setKeyUses(Use.SIGNATURE, null);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").keyUse(Use.SIGNATURE).build());
		keyList.add(new ECKey.Builder(ECKey.Curve.P_256, new Base64URL("x"), new Base64URL("y")).keyID("2").build());
		keyList.add(new ECKey.Builder(ECKey.Curve.P_256, new Base64URL("x"), new Base64URL("y")).keyID("3").keyUse(Use.ENCRYPTION).build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals(KeyType.RSA, key1.getKeyType());
		assertEquals(Use.SIGNATURE, key1.getKeyUse());
		assertEquals("1", key1.getKeyID());

		ECKey key2 = (ECKey)matches.get(1);
		assertEquals(KeyType.EC, key2.getKeyType());
		assertEquals("2", key2.getKeyID());

		assertEquals(2, matches.size());
	}


	public void testMatchAlgorithm() {

		JWKSelector selector = new JWKSelector();
		selector.setAlgorithm(JWSAlgorithm.RS256);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").algorithm(JWSAlgorithm.RS256).build());
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("2").algorithm(JWSAlgorithm.PS256).build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals(KeyType.RSA, key1.getKeyType());
		assertEquals(JWSAlgorithm.RS256, key1.getAlgorithm());
		assertEquals("1", key1.getKeyID());

		assertEquals(1, matches.size());
	}


	public void testMatchID() {

		JWKSelector selector = new JWKSelector();
		selector.setKeyID("1");

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").algorithm(JWSAlgorithm.RS256).build());
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("2").algorithm(JWSAlgorithm.RS256).build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals("1", key1.getKeyID());

		assertEquals(1, matches.size());
	}


	public void testNoMatchesByID() {

		JWKSelector selector = new JWKSelector();
		selector.setKeyID("1");

		RSAKey key = new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("2").build();

		JWKSet jwkSet = new JWKSet(key);

		List<JWK> matches = selector.select(jwkSet);

		assertTrue(matches.isEmpty());
	}


	public void testMatchPrivateOnly() {

		JWKSelector selector = new JWKSelector();
		selector.setPrivateOnly(true);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").algorithm(JWSAlgorithm.RS256).build());
		keyList.add(new OctetSequenceKey.Builder(new Base64URL("k")).build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		OctetSequenceKey key1 = (OctetSequenceKey)matches.get(0);
		assertEquals("k", key1.getKeyValue().toString());

		assertEquals(1, matches.size());
	}


	public void testMatchPublicOnly() {

		JWKSelector selector = new JWKSelector();
		selector.setPublicOnly(true);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").algorithm(JWSAlgorithm.RS256).build());
		keyList.add(new OctetSequenceKey.Builder(new Base64URL("k")).build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals("1", key1.getKeyID());

		assertEquals(1, matches.size());
	}


	public void testMatchComplex() {

		JWKSelector selector = new JWKSelector();
		selector.setKeyType(KeyType.RSA);
		selector.setKeyUse(Use.SIGNATURE);
		selector.setAlgorithm(JWSAlgorithm.RS256);
		selector.setKeyID("1");
		selector.setPublicOnly(true);

		List<JWK> keyList = new ArrayList<JWK>();
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("1").keyUse(Use.SIGNATURE).algorithm(JWSAlgorithm.RS256).build());
		keyList.add(new RSAKey.Builder(new Base64URL("n"), new Base64URL("e")).keyID("2").algorithm(JWSAlgorithm.RS256).build());

		JWKSet jwkSet = new JWKSet(keyList);

		List<JWK> matches = selector.select(jwkSet);

		RSAKey key1 = (RSAKey)matches.get(0);
		assertEquals("1", key1.getKeyID());

		assertEquals(1, matches.size());
	}
}
