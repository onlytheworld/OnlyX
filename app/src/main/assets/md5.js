function d(n, t) {
  var r = (65535 & n) + (65535 & t);
  return (((n >> 16) + (t >> 16) + (r >> 16)) << 16) | (65535 & r);
}
function f(n, t, r, e, o, u) {
  return d(((c = d(d(t, n), d(e, u))) << (f = o)) | (c >>> (32 - f)), r);
  var c, f;
}
function l(n, t, r, e, o, u, c) {
  return f((t & r) | (~t & e), n, t, o, u, c);
}
function v(n, t, r, e, o, u, c) {
  return f((t & e) | (r & ~e), n, t, o, u, c);
}
function g(n, t, r, e, o, u, c) {
  return f(t ^ r ^ e, n, t, o, u, c);
}
function m(n, t, r, e, o, u, c) {
  return f(r ^ (t | ~e), n, t, o, u, c);
}
function i(n, t) {
  var r, e, o, u;
  (n[t >> 5] |= 128 << t % 32), (n[14 + (((t + 64) >>> 9) << 4)] = t);
  for (
    var c = 1732584193, f = -271733879, i = -1732584194, a = 271733878, h = 0;
    h < n.length;
    h += 16
  )
    (c = l((r = c), (e = f), (o = i), (u = a), n[h], 7, -680876936)),
      (a = l(a, c, f, i, n[h + 1], 12, -389564586)),
      (i = l(i, a, c, f, n[h + 2], 17, 606105819)),
      (f = l(f, i, a, c, n[h + 3], 22, -1044525330)),
      (c = l(c, f, i, a, n[h + 4], 7, -176418897)),
      (a = l(a, c, f, i, n[h + 5], 12, 1200080426)),
      (i = l(i, a, c, f, n[h + 6], 17, -1473231341)),
      (f = l(f, i, a, c, n[h + 7], 22, -45705983)),
      (c = l(c, f, i, a, n[h + 8], 7, 1770035416)),
      (a = l(a, c, f, i, n[h + 9], 12, -1958414417)),
      (i = l(i, a, c, f, n[h + 10], 17, -42063)),
      (f = l(f, i, a, c, n[h + 11], 22, -1990404162)),
      (c = l(c, f, i, a, n[h + 12], 7, 1804603682)),
      (a = l(a, c, f, i, n[h + 13], 12, -40341101)),
      (i = l(i, a, c, f, n[h + 14], 17, -1502002290)),
      (c = v(
        c,
        (f = l(f, i, a, c, n[h + 15], 22, 1236535329)),
        i,
        a,
        n[h + 1],
        5,
        -165796510
      )),
      (a = v(a, c, f, i, n[h + 6], 9, -1069501632)),
      (i = v(i, a, c, f, n[h + 11], 14, 643717713)),
      (f = v(f, i, a, c, n[h], 20, -373897302)),
      (c = v(c, f, i, a, n[h + 5], 5, -701558691)),
      (a = v(a, c, f, i, n[h + 10], 9, 38016083)),
      (i = v(i, a, c, f, n[h + 15], 14, -660478335)),
      (f = v(f, i, a, c, n[h + 4], 20, -405537848)),
      (c = v(c, f, i, a, n[h + 9], 5, 568446438)),
      (a = v(a, c, f, i, n[h + 14], 9, -1019803690)),
      (i = v(i, a, c, f, n[h + 3], 14, -187363961)),
      (f = v(f, i, a, c, n[h + 8], 20, 1163531501)),
      (c = v(c, f, i, a, n[h + 13], 5, -1444681467)),
      (a = v(a, c, f, i, n[h + 2], 9, -51403784)),
      (i = v(i, a, c, f, n[h + 7], 14, 1735328473)),
      (c = g(
        c,
        (f = v(f, i, a, c, n[h + 12], 20, -1926607734)),
        i,
        a,
        n[h + 5],
        4,
        -378558
      )),
      (a = g(a, c, f, i, n[h + 8], 11, -2022574463)),
      (i = g(i, a, c, f, n[h + 11], 16, 1839030562)),
      (f = g(f, i, a, c, n[h + 14], 23, -35309556)),
      (c = g(c, f, i, a, n[h + 1], 4, -1530992060)),
      (a = g(a, c, f, i, n[h + 4], 11, 1272893353)),
      (i = g(i, a, c, f, n[h + 7], 16, -155497632)),
      (f = g(f, i, a, c, n[h + 10], 23, -1094730640)),
      (c = g(c, f, i, a, n[h + 13], 4, 681279174)),
      (a = g(a, c, f, i, n[h], 11, -358537222)),
      (i = g(i, a, c, f, n[h + 3], 16, -722521979)),
      (f = g(f, i, a, c, n[h + 6], 23, 76029189)),
      (c = g(c, f, i, a, n[h + 9], 4, -640364487)),
      (a = g(a, c, f, i, n[h + 12], 11, -421815835)),
      (i = g(i, a, c, f, n[h + 15], 16, 530742520)),
      (c = m(
        c,
        (f = g(f, i, a, c, n[h + 2], 23, -995338651)),
        i,
        a,
        n[h],
        6,
        -198630844
      )),
      (a = m(a, c, f, i, n[h + 7], 10, 1126891415)),
      (i = m(i, a, c, f, n[h + 14], 15, -1416354905)),
      (f = m(f, i, a, c, n[h + 5], 21, -57434055)),
      (c = m(c, f, i, a, n[h + 12], 6, 1700485571)),
      (a = m(a, c, f, i, n[h + 3], 10, -1894986606)),
      (i = m(i, a, c, f, n[h + 10], 15, -1051523)),
      (f = m(f, i, a, c, n[h + 1], 21, -2054922799)),
      (c = m(c, f, i, a, n[h + 8], 6, 1873313359)),
      (a = m(a, c, f, i, n[h + 15], 10, -30611744)),
      (i = m(i, a, c, f, n[h + 6], 15, -1560198380)),
      (f = m(f, i, a, c, n[h + 13], 21, 1309151649)),
      (c = m(c, f, i, a, n[h + 4], 6, -145523070)),
      (a = m(a, c, f, i, n[h + 11], 10, -1120210379)),
      (i = m(i, a, c, f, n[h + 2], 15, 718787259)),
      (f = m(f, i, a, c, n[h + 9], 21, -343485551)),
      (c = d(c, r)),
      (f = d(f, e)),
      (i = d(i, o)),
      (a = d(a, u));
  return [c, f, i, a];
}
function a(n) {
  for (var t = "", r = 32 * n.length, e = 0; e < r; e += 8)
    t += String.fromCharCode((n[e >> 5] >>> e % 32) & 255);
  return t;
}
function h(n) {
  var t = [];
  for (t[(n.length >> 2) - 1] = void 0, e = 0; e < t.length; e += 1) t[e] = 0;
  for (var r = 8 * n.length, e = 0; e < r; e += 8)
    t[e >> 5] |= (255 & n.charCodeAt(e / 8)) << e % 32;
  return t;
}
function e(n) {
  for (var t, r = "0123456789abcdef", e = "", o = 0; o < n.length; o += 1)
    (t = n.charCodeAt(o)), (e += r.charAt((t >>> 4) & 15) + r.charAt(15 & t));
  return e;
}
function o(n) {
  return a(i(h(n), 8 * n.length));
}
function md5(n) {
  return e(o(n));
}
function get_num(e, t) {
  var a = 10;
  if (e >= 268850) {
    var n = e + t;
    switch (((n = (n = (n = md5(n)).substr(-1)).charCodeAt()), (n %= 10))) {
      case 0:
        a = 2;
        break;
      case 1:
        a = 4;
        break;
      case 2:
        a = 6;
        break;
      case 3:
        a = 8;
        break;
      case 4:
        a = 10;
        break;
      case 5:
        a = 12;
        break;
      case 6:
        a = 14;
        break;
      case 7:
        a = 16;
        break;
      case 8:
        a = 18;
        break;
      case 9:
        a = 20;
    }
  }
  return a;
}
