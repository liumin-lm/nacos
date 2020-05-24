// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Data.proto

package com.alibaba.nacos.consistency.entity;

/**
 * Protobuf type {@code Log}
 */
public  final class Log extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:Log)
    LogOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Log.newBuilder() to construct.
  private Log(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Log() {
    group_ = "";
    key_ = "";
    data_ = com.google.protobuf.ByteString.EMPTY;
    type_ = "";
    operation_ = "";
  }

  @Override
  @SuppressWarnings({"unused"})
  protected Object newInstance(
      UnusedPrivateParameter unused) {
    return new Log();
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Log(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            String s = input.readStringRequireUtf8();

            group_ = s;
            break;
          }
          case 18: {
            String s = input.readStringRequireUtf8();

            key_ = s;
            break;
          }
          case 26: {

            data_ = input.readBytes();
            break;
          }
          case 34: {
            String s = input.readStringRequireUtf8();

            type_ = s;
            break;
          }
          case 42: {
            String s = input.readStringRequireUtf8();

            operation_ = s;
            break;
          }
          case 50: {
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              extendInfo_ = com.google.protobuf.MapField.newMapField(
                  ExtendInfoDefaultEntryHolder.defaultEntry);
              mutable_bitField0_ |= 0x00000001;
            }
            com.google.protobuf.MapEntry<String, String>
            extendInfo__ = input.readMessage(
                ExtendInfoDefaultEntryHolder.defaultEntry.getParserForType(), extensionRegistry);
            extendInfo_.getMutableMap().put(
                extendInfo__.getKey(), extendInfo__.getValue());
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return Data.internal_static_Log_descriptor;
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  protected com.google.protobuf.MapField internalGetMapField(
      int number) {
    switch (number) {
      case 6:
        return internalGetExtendInfo();
      default:
        throw new RuntimeException(
            "Invalid map field number: " + number);
    }
  }
  @Override
  protected FieldAccessorTable
      internalGetFieldAccessorTable() {
    return Data.internal_static_Log_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            Log.class, Log.Builder.class);
  }

  public static final int GROUP_FIELD_NUMBER = 1;
  private volatile Object group_;
  /**
   * <code>string group = 1;</code>
   */
  public String getGroup() {
    Object ref = group_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      group_ = s;
      return s;
    }
  }
  /**
   * <code>string group = 1;</code>
   */
  public com.google.protobuf.ByteString
      getGroupBytes() {
    Object ref = group_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      group_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int KEY_FIELD_NUMBER = 2;
  private volatile Object key_;
  /**
   * <code>string key = 2;</code>
   */
  public String getKey() {
    Object ref = key_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      key_ = s;
      return s;
    }
  }
  /**
   * <code>string key = 2;</code>
   */
  public com.google.protobuf.ByteString
      getKeyBytes() {
    Object ref = key_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      key_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int DATA_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString data_;
  /**
   * <code>bytes data = 3;</code>
   */
  public com.google.protobuf.ByteString getData() {
    return data_;
  }

  public static final int TYPE_FIELD_NUMBER = 4;
  private volatile Object type_;
  /**
   * <code>string type = 4;</code>
   */
  public String getType() {
    Object ref = type_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      type_ = s;
      return s;
    }
  }
  /**
   * <code>string type = 4;</code>
   */
  public com.google.protobuf.ByteString
      getTypeBytes() {
    Object ref = type_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      type_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int OPERATION_FIELD_NUMBER = 5;
  private volatile Object operation_;
  /**
   * <code>string operation = 5;</code>
   */
  public String getOperation() {
    Object ref = operation_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      operation_ = s;
      return s;
    }
  }
  /**
   * <code>string operation = 5;</code>
   */
  public com.google.protobuf.ByteString
      getOperationBytes() {
    Object ref = operation_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      operation_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int EXTENDINFO_FIELD_NUMBER = 6;
  private static final class ExtendInfoDefaultEntryHolder {
    static final com.google.protobuf.MapEntry<
        String, String> defaultEntry =
            com.google.protobuf.MapEntry
            .<String, String>newDefaultInstance(
                Data.internal_static_Log_ExtendInfoEntry_descriptor,
                com.google.protobuf.WireFormat.FieldType.STRING,
                "",
                com.google.protobuf.WireFormat.FieldType.STRING,
                "");
  }
  private com.google.protobuf.MapField<
      String, String> extendInfo_;
  private com.google.protobuf.MapField<String, String>
  internalGetExtendInfo() {
    if (extendInfo_ == null) {
      return com.google.protobuf.MapField.emptyMapField(
          ExtendInfoDefaultEntryHolder.defaultEntry);
    }
    return extendInfo_;
  }

  public int getExtendInfoCount() {
    return internalGetExtendInfo().getMap().size();
  }
  /**
   * <code>map&lt;string, string&gt; extendInfo = 6;</code>
   */

  public boolean containsExtendInfo(
      String key) {
    if (key == null) { throw new NullPointerException(); }
    return internalGetExtendInfo().getMap().containsKey(key);
  }
  /**
   * Use {@link #getExtendInfoMap()} instead.
   */
  @Deprecated
  public java.util.Map<String, String> getExtendInfo() {
    return getExtendInfoMap();
  }
  /**
   * <code>map&lt;string, string&gt; extendInfo = 6;</code>
   */

  public java.util.Map<String, String> getExtendInfoMap() {
    return internalGetExtendInfo().getMap();
  }
  /**
   * <code>map&lt;string, string&gt; extendInfo = 6;</code>
   */

  public String getExtendInfoOrDefault(
      String key,
      String defaultValue) {
    if (key == null) { throw new NullPointerException(); }
    java.util.Map<String, String> map =
        internalGetExtendInfo().getMap();
    return map.containsKey(key) ? map.get(key) : defaultValue;
  }
  /**
   * <code>map&lt;string, string&gt; extendInfo = 6;</code>
   */

  public String getExtendInfoOrThrow(
      String key) {
    if (key == null) { throw new NullPointerException(); }
    java.util.Map<String, String> map =
        internalGetExtendInfo().getMap();
    if (!map.containsKey(key)) {
      throw new IllegalArgumentException();
    }
    return map.get(key);
  }

  private byte memoizedIsInitialized = -1;
  @Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getGroupBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, group_);
    }
    if (!getKeyBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, key_);
    }
    if (!data_.isEmpty()) {
      output.writeBytes(3, data_);
    }
    if (!getTypeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, type_);
    }
    if (!getOperationBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, operation_);
    }
    com.google.protobuf.GeneratedMessageV3
      .serializeStringMapTo(
        output,
        internalGetExtendInfo(),
        ExtendInfoDefaultEntryHolder.defaultEntry,
        6);
    unknownFields.writeTo(output);
  }

  @Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getGroupBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, group_);
    }
    if (!getKeyBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, key_);
    }
    if (!data_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(3, data_);
    }
    if (!getTypeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, type_);
    }
    if (!getOperationBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, operation_);
    }
    for (java.util.Map.Entry<String, String> entry
         : internalGetExtendInfo().getMap().entrySet()) {
      com.google.protobuf.MapEntry<String, String>
      extendInfo__ = ExtendInfoDefaultEntryHolder.defaultEntry.newBuilderForType()
          .setKey(entry.getKey())
          .setValue(entry.getValue())
          .build();
      size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(6, extendInfo__);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof Log)) {
      return super.equals(obj);
    }
    Log other = (Log) obj;

    if (!getGroup()
        .equals(other.getGroup())) return false;
    if (!getKey()
        .equals(other.getKey())) return false;
    if (!getData()
        .equals(other.getData())) return false;
    if (!getType()
        .equals(other.getType())) return false;
    if (!getOperation()
        .equals(other.getOperation())) return false;
    if (!internalGetExtendInfo().equals(
        other.internalGetExtendInfo())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + GROUP_FIELD_NUMBER;
    hash = (53 * hash) + getGroup().hashCode();
    hash = (37 * hash) + KEY_FIELD_NUMBER;
    hash = (53 * hash) + getKey().hashCode();
    hash = (37 * hash) + DATA_FIELD_NUMBER;
    hash = (53 * hash) + getData().hashCode();
    hash = (37 * hash) + TYPE_FIELD_NUMBER;
    hash = (53 * hash) + getType().hashCode();
    hash = (37 * hash) + OPERATION_FIELD_NUMBER;
    hash = (53 * hash) + getOperation().hashCode();
    if (!internalGetExtendInfo().getMap().isEmpty()) {
      hash = (37 * hash) + EXTENDINFO_FIELD_NUMBER;
      hash = (53 * hash) + internalGetExtendInfo().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static Log parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static Log parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static Log parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static Log parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static Log parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static Log parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static Log parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static Log parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static Log parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static Log parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static Log parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static Log parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(Log prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @Override
  protected Builder newBuilderForType(
      BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code Log}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Log)
      LogOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return Data.internal_static_Log_descriptor;
    }

    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMapField(
        int number) {
      switch (number) {
        case 6:
          return internalGetExtendInfo();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @SuppressWarnings({"rawtypes"})
    protected com.google.protobuf.MapField internalGetMutableMapField(
        int number) {
      switch (number) {
        case 6:
          return internalGetMutableExtendInfo();
        default:
          throw new RuntimeException(
              "Invalid map field number: " + number);
      }
    }
    @Override
    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return Data.internal_static_Log_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              Log.class, Log.Builder.class);
    }

    // Construct using com.alibaba.nacos.consistency.entity.Log.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @Override
    public Builder clear() {
      super.clear();
      group_ = "";

      key_ = "";

      data_ = com.google.protobuf.ByteString.EMPTY;

      type_ = "";

      operation_ = "";

      internalGetMutableExtendInfo().clear();
      return this;
    }

    @Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return Data.internal_static_Log_descriptor;
    }

    @Override
    public Log getDefaultInstanceForType() {
      return Log.getDefaultInstance();
    }

    @Override
    public Log build() {
      Log result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @Override
    public Log buildPartial() {
      Log result = new Log(this);
      int from_bitField0_ = bitField0_;
      result.group_ = group_;
      result.key_ = key_;
      result.data_ = data_;
      result.type_ = type_;
      result.operation_ = operation_;
      result.extendInfo_ = internalGetExtendInfo();
      result.extendInfo_.makeImmutable();
      onBuilt();
      return result;
    }

    @Override
    public Builder clone() {
      return super.clone();
    }
    @Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.setField(field, value);
    }
    @Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return super.addRepeatedField(field, value);
    }
    @Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof Log) {
        return mergeFrom((Log)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(Log other) {
      if (other == Log.getDefaultInstance()) return this;
      if (!other.getGroup().isEmpty()) {
        group_ = other.group_;
        onChanged();
      }
      if (!other.getKey().isEmpty()) {
        key_ = other.key_;
        onChanged();
      }
      if (other.getData() != com.google.protobuf.ByteString.EMPTY) {
        setData(other.getData());
      }
      if (!other.getType().isEmpty()) {
        type_ = other.type_;
        onChanged();
      }
      if (!other.getOperation().isEmpty()) {
        operation_ = other.operation_;
        onChanged();
      }
      internalGetMutableExtendInfo().mergeFrom(
          other.internalGetExtendInfo());
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @Override
    public final boolean isInitialized() {
      return true;
    }

    @Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Log parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (Log) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private Object group_ = "";
    /**
     * <code>string group = 1;</code>
     */
    public String getGroup() {
      Object ref = group_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        group_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>string group = 1;</code>
     */
    public com.google.protobuf.ByteString
        getGroupBytes() {
      Object ref = group_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        group_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string group = 1;</code>
     */
    public Builder setGroup(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }

      group_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string group = 1;</code>
     */
    public Builder clearGroup() {

      group_ = getDefaultInstance().getGroup();
      onChanged();
      return this;
    }
    /**
     * <code>string group = 1;</code>
     */
    public Builder setGroupBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

      group_ = value;
      onChanged();
      return this;
    }

    private Object key_ = "";
    /**
     * <code>string key = 2;</code>
     */
    public String getKey() {
      Object ref = key_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        key_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>string key = 2;</code>
     */
    public com.google.protobuf.ByteString
        getKeyBytes() {
      Object ref = key_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        key_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string key = 2;</code>
     */
    public Builder setKey(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }

      key_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string key = 2;</code>
     */
    public Builder clearKey() {

      key_ = getDefaultInstance().getKey();
      onChanged();
      return this;
    }
    /**
     * <code>string key = 2;</code>
     */
    public Builder setKeyBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

      key_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes data = 3;</code>
     */
    public com.google.protobuf.ByteString getData() {
      return data_;
    }
    /**
     * <code>bytes data = 3;</code>
     */
    public Builder setData(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }

      data_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bytes data = 3;</code>
     */
    public Builder clearData() {

      data_ = getDefaultInstance().getData();
      onChanged();
      return this;
    }

    private Object type_ = "";
    /**
     * <code>string type = 4;</code>
     */
    public String getType() {
      Object ref = type_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        type_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>string type = 4;</code>
     */
    public com.google.protobuf.ByteString
        getTypeBytes() {
      Object ref = type_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        type_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string type = 4;</code>
     */
    public Builder setType(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }

      type_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string type = 4;</code>
     */
    public Builder clearType() {

      type_ = getDefaultInstance().getType();
      onChanged();
      return this;
    }
    /**
     * <code>string type = 4;</code>
     */
    public Builder setTypeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

      type_ = value;
      onChanged();
      return this;
    }

    private Object operation_ = "";
    /**
     * <code>string operation = 5;</code>
     */
    public String getOperation() {
      Object ref = operation_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        operation_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>string operation = 5;</code>
     */
    public com.google.protobuf.ByteString
        getOperationBytes() {
      Object ref = operation_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        operation_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string operation = 5;</code>
     */
    public Builder setOperation(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }

      operation_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string operation = 5;</code>
     */
    public Builder clearOperation() {

      operation_ = getDefaultInstance().getOperation();
      onChanged();
      return this;
    }
    /**
     * <code>string operation = 5;</code>
     */
    public Builder setOperationBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

      operation_ = value;
      onChanged();
      return this;
    }

    private com.google.protobuf.MapField<
        String, String> extendInfo_;
    private com.google.protobuf.MapField<String, String>
    internalGetExtendInfo() {
      if (extendInfo_ == null) {
        return com.google.protobuf.MapField.emptyMapField(
            ExtendInfoDefaultEntryHolder.defaultEntry);
      }
      return extendInfo_;
    }
    private com.google.protobuf.MapField<String, String>
    internalGetMutableExtendInfo() {
      onChanged();
      if (extendInfo_ == null) {
        extendInfo_ = com.google.protobuf.MapField.newMapField(
            ExtendInfoDefaultEntryHolder.defaultEntry);
      }
      if (!extendInfo_.isMutable()) {
        extendInfo_ = extendInfo_.copy();
      }
      return extendInfo_;
    }

    public int getExtendInfoCount() {
      return internalGetExtendInfo().getMap().size();
    }
    /**
     * <code>map&lt;string, string&gt; extendInfo = 6;</code>
     */

    public boolean containsExtendInfo(
        String key) {
      if (key == null) { throw new NullPointerException(); }
      return internalGetExtendInfo().getMap().containsKey(key);
    }
    /**
     * Use {@link #getExtendInfoMap()} instead.
     */
    @Deprecated
    public java.util.Map<String, String> getExtendInfo() {
      return getExtendInfoMap();
    }
    /**
     * <code>map&lt;string, string&gt; extendInfo = 6;</code>
     */

    public java.util.Map<String, String> getExtendInfoMap() {
      return internalGetExtendInfo().getMap();
    }
    /**
     * <code>map&lt;string, string&gt; extendInfo = 6;</code>
     */

    public String getExtendInfoOrDefault(
        String key,
        String defaultValue) {
      if (key == null) { throw new NullPointerException(); }
      java.util.Map<String, String> map =
          internalGetExtendInfo().getMap();
      return map.containsKey(key) ? map.get(key) : defaultValue;
    }
    /**
     * <code>map&lt;string, string&gt; extendInfo = 6;</code>
     */

    public String getExtendInfoOrThrow(
        String key) {
      if (key == null) { throw new NullPointerException(); }
      java.util.Map<String, String> map =
          internalGetExtendInfo().getMap();
      if (!map.containsKey(key)) {
        throw new IllegalArgumentException();
      }
      return map.get(key);
    }

    public Builder clearExtendInfo() {
      internalGetMutableExtendInfo().getMutableMap()
          .clear();
      return this;
    }
    /**
     * <code>map&lt;string, string&gt; extendInfo = 6;</code>
     */

    public Builder removeExtendInfo(
        String key) {
      if (key == null) { throw new NullPointerException(); }
      internalGetMutableExtendInfo().getMutableMap()
          .remove(key);
      return this;
    }
    /**
     * Use alternate mutation accessors instead.
     */
    @Deprecated
    public java.util.Map<String, String>
    getMutableExtendInfo() {
      return internalGetMutableExtendInfo().getMutableMap();
    }
    /**
     * <code>map&lt;string, string&gt; extendInfo = 6;</code>
     */
    public Builder putExtendInfo(
        String key,
        String value) {
      if (key == null) { throw new NullPointerException(); }
      if (value == null) { throw new NullPointerException(); }
      internalGetMutableExtendInfo().getMutableMap()
          .put(key, value);
      return this;
    }
    /**
     * <code>map&lt;string, string&gt; extendInfo = 6;</code>
     */

    public Builder putAllExtendInfo(
        java.util.Map<String, String> values) {
      internalGetMutableExtendInfo().getMutableMap()
          .putAll(values);
      return this;
    }
    @Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:Log)
  }

  // @@protoc_insertion_point(class_scope:Log)
  private static final Log DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new Log();
  }

  public static Log getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Log>
      PARSER = new com.google.protobuf.AbstractParser<Log>() {
    @Override
    public Log parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Log(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Log> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<Log> getParserForType() {
    return PARSER;
  }

  @Override
  public Log getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

