#!/usr/bin/perl

use CGI;
use DBI;
use DBD::mysql;

my $dbh=DBI->connect("DBI:mysql:database=emcmarket;host=localhost", "emcmarket", "NE6xYRe6UPwW3Ray");

my $cgi=CGI->new;
my $rows=0;
if ($cgi->param("upload")) {
	my $sthdel=$dbh->prepare(qq(
		delete from signs
		where server=? and x=? and y=? and z=?
	));
	my $sthins=$dbh->prepare(qq(
		insert into signs (server, x, y, z, amount, buy, sell, owner, item)
		values (?, ?, ?, ?, ?, ?, ?, ?, ?)
	));
	foreach my $row (split("\n", $cgi->param("upload"))) {
		my ($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item)=
			split(":", $row);
		$sthdel->execute($server, $x, $y, $z);
		$sthins->execute($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item);
		$rows++;
	}
}

print qq(Content-type: text/plain\n);
print qq(\n);
print "$rows rows uploaded.\n";
