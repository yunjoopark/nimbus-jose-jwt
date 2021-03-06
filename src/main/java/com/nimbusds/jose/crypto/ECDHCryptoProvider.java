/*
 * nimbus-jose-jwt
 *
 * Copyright 2012-2016, Connect2id Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.nimbusds.jose.crypto;


import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.jwk.Curve;


/**
 * The base abstract class for Elliptic Curve Diffie-Hellman encrypters and
 * decrypters of {@link com.nimbusds.jose.JWEObject JWE objects}.
 *
 * <p>Supports the following key management algorithms:
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.JWEAlgorithm#ECDH_ES}
 *     <li>{@link com.nimbusds.jose.JWEAlgorithm#ECDH_ES_A128KW}
 *     <li>{@link com.nimbusds.jose.JWEAlgorithm#ECDH_ES_A192KW}
 *     <li>{@link com.nimbusds.jose.JWEAlgorithm#ECDH_ES_A256KW}
 * </ul>
 *
 * <p>Supports the following elliptic curves:
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.jwk.Curve#P_256}
 *     <li>{@link com.nimbusds.jose.jwk.Curve#P_384}
 *     <li>{@link com.nimbusds.jose.jwk.Curve#P_521}
 * </ul>
 *
 * <p>Supports the following content encryption algorithms:
 *
 * <ul>
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128CBC_HS256}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A192CBC_HS384}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256CBC_HS512}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128GCM}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A192GCM}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256GCM}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A128CBC_HS256_DEPRECATED}
 *     <li>{@link com.nimbusds.jose.EncryptionMethod#A256CBC_HS512_DEPRECATED}
 * </ul>
 *
 * @author Vladimir Dzhuvinov
 * @version 2015-05-26
 */
abstract class ECDHCryptoProvider extends BaseJWEProvider {


	/**
	 * The supported JWE algorithms by the ECDH crypto provider class.
	 */
	public static final Set<JWEAlgorithm> SUPPORTED_ALGORITHMS;


	/**
	 * The supported encryption methods by the ECDH crypto provider class.
	 */
	public static final Set<EncryptionMethod> SUPPORTED_ENCRYPTION_METHODS = ContentCryptoProvider.SUPPORTED_ENCRYPTION_METHODS;


	/**
	 * The supported EC JWK curves by the ECDH crypto provider class.
	 */
	public static final Set<Curve> SUPPORTED_ELLIPTIC_CURVES;


	static {
		Set<JWEAlgorithm> algs = new LinkedHashSet<>();
		algs.add(JWEAlgorithm.ECDH_ES);
		algs.add(JWEAlgorithm.ECDH_ES_A128KW);
		algs.add(JWEAlgorithm.ECDH_ES_A192KW);
		algs.add(JWEAlgorithm.ECDH_ES_A256KW);
		SUPPORTED_ALGORITHMS = Collections.unmodifiableSet(algs);

		Set<Curve> curves = new LinkedHashSet<>();
		curves.add(Curve.P_256);
		curves.add(Curve.P_384);
		curves.add(Curve.P_521);
		SUPPORTED_ELLIPTIC_CURVES = Collections.unmodifiableSet(curves);
	}


	/**
	 * The elliptic curve.
	 */
	private final Curve curve;


	/**
	 * The Concatenation Key Derivation Function (KDF).
	 */
	private final ConcatKDF concatKDF;


	/**
	 * Creates a new Elliptic Curve Diffie-Hellman encryption /decryption
	 * provider.
	 *
	 * @param curve The elliptic curve. Must be supported and not
	 *              {@code null}.
	 *
	 * @throws JOSEException If the elliptic curve is not supported.
	 */
	protected ECDHCryptoProvider(final Curve curve)
		throws JOSEException {

		super(SUPPORTED_ALGORITHMS, ContentCryptoProvider.SUPPORTED_ENCRYPTION_METHODS);

		Curve definedCurve = curve != null ? curve : new Curve("unknown");

		if (! SUPPORTED_ELLIPTIC_CURVES.contains(curve)) {
			throw new JOSEException(AlgorithmSupportMessage.unsupportedEllipticCurve(
				definedCurve, SUPPORTED_ELLIPTIC_CURVES));
		}

		this.curve = curve;

		concatKDF = new ConcatKDF("SHA-256");
	}


	/**
	 * Returns the Concatenation Key Derivation Function (KDF).
	 *
	 * @return The concat KDF.
	 */
	protected ConcatKDF getConcatKDF() {

		return concatKDF;
	}


	/**
	 * Returns the names of the supported elliptic curves. These correspond
	 * to the {@code crv} EC JWK parameter.
	 *
	 * @return The supported elliptic curves.
	 */
	public Set<Curve> supportedEllipticCurves() {

		return SUPPORTED_ELLIPTIC_CURVES;
	}


	/**
	 * Returns the elliptic curve of the key (JWK designation).
	 *
	 * @return The elliptic curve.
	 */
	public Curve getCurve() {

		return curve;
	}
}