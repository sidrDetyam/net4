package ru.nsu.gemuev.net4.model.communication;

import lombok.NonNull;
import org.jetbrains.annotations.Range;
import ru.nsu.gemuev.net4.model.NodeRole;

import java.net.InetAddress;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


public class NodesRepository {

    private final List<Node> nodes = new ArrayList<>();

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public Optional<Node> findNodeByAddress(@NonNull InetAddress address,
                                            @Range(from = 0, to = 65536) int port) {
        return nodes.stream()
                .filter(player -> address.equals(player.getAddress()) && player.getPort() == port)
                .findAny();
    }

    public Optional<Node> findNodeById(int playerId) {
        return nodes.stream()
                .filter(player -> player.getPlayerId() == playerId)
                .findAny();
    }

    public Optional<Node> findNodeByRole(@NonNull NodeRole role) {
        return nodes.stream()
                .filter(player -> role.equals(player.getRole()))
                .findAny();
    }

    public void updateLastCommunication(@NonNull InetAddress address,
                                        @Range(from = 0, to = 65536) int port) {
        findNodeByAddress(address, port).ifPresent(Node::updateLastCommunication);
    }

    public void setNodes(@NonNull Collection<? extends Node> nodes) {
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }

    public void addNode(@NonNull Node node) {
        nodes.add(node);
    }

    public void delete(@NonNull Node node){
        nodes.remove(node);
    }

    public void deleteAll(@NonNull Collection<? extends Node> nodeCol){
        nodes.removeAll(nodeCol);
    }

    public List<Node> findExpired(@Range(from = 0, to = Long.MAX_VALUE) long ttl, int myId){
        long instant = Instant.now().toEpochMilli();
        return nodes.stream()
                .filter(node -> node.getPlayerId() != myId)
                .filter(node -> instant - node.getLastCommunication() > ttl)
                .collect(Collectors.toList());
    }
}
