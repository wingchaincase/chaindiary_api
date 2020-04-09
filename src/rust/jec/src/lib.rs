use ecies::{decrypt, encrypt};
use hex::{FromHex, ToHex};
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;
use signatory::ecdsa::curve::secp256k1::FixedSignature;
use signatory::signature::{Signature, Signer, Verifier};
use signatory_secp256k1::{EcdsaSigner, EcdsaVerifier, PublicKey, SecretKey};

#[no_mangle]
pub extern "system" fn Java_wingchaincase_chaindiaryapi_service_EcService_eciesEncrypt(
	env: JNIEnv,
	_jclass: JClass,
	public_key_hex: JString,
	plain_hex: JString,
) -> jstring {
	let public_key_hex: String = env
		.get_string(public_key_hex)
		.expect("pass var error")
		.into();

	let plain_hex: String = env.get_string(plain_hex).expect("pass var error").into();

	let cipher_hex = ecies_enc(&public_key_hex, &plain_hex).expect("enc error");

	let cipher_hex = env.new_string(cipher_hex).expect("build str error");

	cipher_hex.into_inner()
}

#[no_mangle]
pub extern "system" fn Java_wingchaincase_chaindiaryapi_service_EcService_eciesDecrypt(
	env: JNIEnv,
	_jclass: JClass,
	secret_key_hex: JString,
	cipher_hex: JString,
) -> jstring {
	let secret_key_hex: String = env
		.get_string(secret_key_hex)
		.expect("pass var error")
		.into();

	let cipher_hex: String = env.get_string(cipher_hex).expect("pass var error").into();

	let plain_hex = ecies_dec(&secret_key_hex, &cipher_hex).expect("dec error");

	let plain_hex = env.new_string(plain_hex).expect("build str error");

	plain_hex.into_inner()
}

#[no_mangle]
pub extern "system" fn Java_wingchaincase_chaindiaryapi_service_EcService_ecdsaVerify(
	env: JNIEnv,
	_jclass: JClass,
	public_key_hex: JString,
	message_hex: JString,
	signature_hex: JString,
) -> bool {
	let public_key_hex: String = env
		.get_string(public_key_hex)
		.expect("pass var error")
		.into();

	let message_hex: String = env.get_string(message_hex).expect("pass var error").into();

	let signature_hex: String = env.get_string(signature_hex).expect("pass var error").into();

	let verified = ecdsa_verify(&public_key_hex, &message_hex, &signature_hex).is_ok();

	verified
}

#[no_mangle]
pub extern "system" fn Java_wingchaincase_chaindiaryapi_service_EcService_ecdsaSign(
	env: JNIEnv,
	_jclass: JClass,
	secret_key_hex: JString,
	message_hex: JString,
) -> jstring {
	let secret_key_hex: String = env
		.get_string(secret_key_hex)
		.expect("pass var error")
		.into();

	let message_hex: String = env.get_string(message_hex).expect("pass var error").into();

	let signature_hex = ecdsa_sign(&secret_key_hex, &message_hex).expect("sign error");

	let signature_hex = env.new_string(signature_hex).expect("build str error");

	signature_hex.into_inner()
}

pub fn ecies_enc(public_key_hex: &str, plain_hex: &str) -> Result<String, String> {
	let public_key = Vec::<u8>::from_hex(public_key_hex).map_err(|_| "Invalid public key")?;

	let plain = Vec::<u8>::from_hex(plain_hex).map_err(|_| "Invalid plain")?;

	let cipher = encrypt(&public_key, &plain).map_err(|_| "Encrypt failed")?;

	let cipher: String = cipher.encode_hex();

	Ok(cipher)
}

pub fn ecies_dec(secret_key_hex: &str, cipher_hex: &str) -> Result<String, String> {
	let secret_key = Vec::<u8>::from_hex(secret_key_hex).map_err(|_| "Invalid secret key")?;

	let cipher = Vec::<u8>::from_hex(cipher_hex).map_err(|_| "Invalid plain")?;

	let plain = decrypt(&secret_key, &cipher).map_err(|_| "Decrypt failed")?;

	let plain: String = plain.encode_hex();

	Ok(plain)
}

pub fn ecdsa_sign(secret_key_hex: &str, message_hex: &str) -> Result<String, String> {
	let secret_key = Vec::<u8>::from_hex(secret_key_hex).map_err(|_| "Invalid secret key")?;
	let message = Vec::<u8>::from_hex(message_hex).map_err(|_| "Invalid secret key")?;

	let secret_key =
		SecretKey::from_bytes(secret_key).map_err(|e| format!("Invalid secret key: {}", e))?;

	let signer = EcdsaSigner::from(&secret_key);

	let signature: FixedSignature = signer.sign(&message);

	let signature: String = signature.as_ref().encode_hex();

	Ok(signature)
}

pub fn ecdsa_verify(public_key_hex: &str, message_hex: &str, signature_hex: &str) -> Result<(), String> {
	let public_key = Vec::<u8>::from_hex(public_key_hex).map_err(|_| "Invalid public key")?;
	let message = Vec::<u8>::from_hex(message_hex).map_err(|_| "Invalid message")?;
	let signature = Vec::<u8>::from_hex(signature_hex).map_err(|_| "Invalid signature")?;

	let public_key = PublicKey::from_bytes(public_key).ok_or("Invalid public key")?;

	let signature = FixedSignature::from_bytes(&signature)
		.map_err(|e| format!("Invalid signature: {}", e))?;

	let verifier = EcdsaVerifier::from(&public_key);

	verifier.verify(&message, &signature).map_err(|_| "Verification failed")?;

	Ok(())
}

#[cfg(test)]
mod tests {
	use super::*;

	#[test]
	fn test_ecies_dec() {
		let expected: String = b"helloworld".to_vec().encode_hex();

		let plain = ecies_dec("1164541ee452e4e288a7a64a31836dadbdfe65417a51a97b719c51349731ce76",
							  "0421960818afc9bf4cfc79547fee6ddd0b8ec68b30828c04ffeeacc80520887c2f2c5877119dc0f2b4555616b419eb1599ca520696d6b0c9411744a83855ca46ab6006b7e4f29710ea7096b86d7a8a1761f7148dc10458f55883e8a13b3ce798d90eeaa30ea3a394c6a210").unwrap();

		assert_eq!(plain, expected);
	}

	#[test]
	fn test_ecies_enc() {
		let cipher = ecies_enc("03391aa7238b79e1aad1e038c95306171a8ac7499357dc99586f96c5f3b9618d60", "616263").unwrap();

		let plain = ecies_dec("9cb4f775e9b67118242cea15285555c287a7e3d2f86ba238c1fe87284b898e9a", &cipher).unwrap();

		assert_eq!(plain, "616263".to_string());
	}

	#[test]
	fn test_ecies_sign() {
		assert_eq!(ecdsa_sign("9cb4f775e9b67118242cea15285555c287a7e3d2f86ba238c1fe87284b898e9a", "616263"),
				   Ok("7c77b65a27984b0e124a0ae2eec6bbf2b338a5c999b943abda576108f92e95364b0b983da055493c87fd138fe5673992b2a48ef85d9ad30c98fc1afcc5fc7bc0".to_string()))
	}

	#[test]
	fn test_ecies_verify() {
		assert_eq!(ecdsa_verify("03391aa7238b79e1aad1e038c95306171a8ac7499357dc99586f96c5f3b9618d60",
								"616263",
								"7c77b65a27984b0e124a0ae2eec6bbf2b338a5c999b943abda576108f92e95364b0b983da055493c87fd138fe5673992b2a48ef85d9ad30c98fc1afcc5fc7bc0"),
				   Ok(()));
	}
}
